#  Copyright 2013-2026 consulo.io
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import os

import gdb

BYTE_STRINGS = {"ANSISTRING", "RAWBYTESTRING", "UTF8STRING"}
WIDE_STRINGS = {"UNICODESTRING", "WIDESTRING"}

CP_ACP = 0
CP_UTF8 = 65001
CP_NONE = 0xFFFF
CODE_PAGES = {
    1200: "utf-16-le",
    1201: "utf-16-be",
    20127: "ascii",
    28591: "latin-1",
}

MAX_LENGTH = 1 << 24


def size_int():
    return gdb.lookup_type("void").pointer().sizeof


def byte_order():
    return "big" if "big endian" in gdb.execute("show endian", to_string=True) else "little"


def read_int(address, size, signed):
    data = bytes(gdb.selected_inferior().read_memory(address, size))
    return int.from_bytes(data, byte_order(), signed=signed)


def element_limit(length):
    limit = gdb.parameter("print elements")
    if limit is None or limit <= 0:
        return length
    return min(length, limit)


def encoding_of(code_page):
    if code_page in (CP_ACP, CP_UTF8, CP_NONE):
        return "utf-8"
    if code_page in CODE_PAGES:
        return CODE_PAGES[code_page]
    return "cp%d" % code_page


def decode(data, encoding):
    try:
        return data.decode(encoding, errors="replace")
    except LookupError:
        return data.decode("utf-8", errors="replace")


def quote(text, truncated):
    return "'" + text.replace("'", "''") + "'" + ("..." if truncated else "")


def type_name(val):
    name = val.type.name or val.type.strip_typedefs().name
    return name.upper() if name else None


class StringPrinter:
    def __init__(self, val, kind):
        self.val = val
        self.kind = kind

    def to_string(self):
        val = self.val
        value_type = val.type.strip_typedefs()
        if self.kind == "SHORTSTRING":
            return self.short_string(val, value_type)
        char_size = value_type.target().sizeof
        if value_type.code == gdb.TYPE_CODE_PTR:
            address = int(val)
            if address == 0:
                return "''"
            if self.kind == "WIDESTRING" and os.name == "nt":
                length = read_int(address - 4, 4, False) // 2
            else:
                length = read_int(address - size_int(), size_int(), True)
        else:
            low, high = value_type.range()
            length = high - low + 1
            if length <= 0:
                return "''"
            address = int(val[low].address)
        if length < 0 or length > MAX_LENGTH:
            return "0x%x" % address
        shown = element_limit(length)
        data = bytes(gdb.selected_inferior().read_memory(address, shown * char_size))
        if char_size == 2:
            encoding = "utf-16-le" if byte_order() == "little" else "utf-16-be"
        elif self.kind == "UTF8STRING":
            encoding = "utf-8"
        else:
            encoding = encoding_of(read_int(address - 3 * size_int(), 2, False))
        return quote(decode(data, encoding), shown < length)

    @staticmethod
    def short_string(val, value_type):
        if value_type.code == gdb.TYPE_CODE_STRUCT:
            fields = value_type.fields()
            length = int(val[fields[0]])
            address = int(val[fields[1]].address)
        else:
            low, high = value_type.range()
            length = high - low + 1
            address = int(val[low].address) if length > 0 else 0
        if length <= 0:
            return "''"
        shown = element_limit(length)
        data = bytes(gdb.selected_inferior().read_memory(address, shown))
        return quote(decode(data, "utf-8"), shown < length)


class ArrayPrinter:
    def __init__(self, address, element_type, length):
        self.address = address
        self.element_type = element_type
        self.length = length

    def to_string(self):
        if self.address == 0:
            return "nil"
        return "length = %d" % self.length

    def children(self):
        if self.address == 0:
            return
        pointer = gdb.Value(self.address).cast(self.element_type.pointer())
        for index in range(element_limit(self.length)):
            yield "[%d]" % index, (pointer + index).dereference()

    def display_hint(self):
        return "array"


def dynamic_array(val):
    value_type = val.type.strip_typedefs()
    array = value_type.target().strip_typedefs()
    address = int(val)
    length = 0 if address == 0 else read_int(address - size_int(), size_int(), True) + 1
    if length < 0 or length > MAX_LENGTH:
        return None
    return ArrayPrinter(address, array.target(), length)


def open_array(val):
    if val.address is None:
        return None
    try:
        frame = gdb.selected_frame()
        block = frame.block()
    except (gdb.error, RuntimeError):
        return None
    address = int(val.address)
    while block is not None:
        for symbol in block:
            if not symbol.is_argument or symbol.type.strip_typedefs().code != gdb.TYPE_CODE_ARRAY:
                continue
            try:
                value = frame.read_var(symbol, block)
            except (gdb.error, ValueError):
                continue
            if value.address is None or int(value.address) != address:
                continue
            for name in ("$high" + symbol.name.upper(), "high" + symbol.name.upper(), "$high" + symbol.name, "high" + symbol.name):
                try:
                    high = int(frame.read_var(name, block))
                except (gdb.error, ValueError):
                    continue
                return ArrayPrinter(address, val.type.strip_typedefs().target(), high + 1)
            return None
        if block.function is not None:
            break
        block = block.superblock
    return None


def lookup(val):
    value_type = val.type.strip_typedefs()
    name = type_name(val)
    if name == "SHORTSTRING":
        if value_type.code in (gdb.TYPE_CODE_STRUCT, gdb.TYPE_CODE_ARRAY):
            return StringPrinter(val, name)
        return None
    if name in BYTE_STRINGS or name in WIDE_STRINGS:
        if value_type.code in (gdb.TYPE_CODE_PTR, gdb.TYPE_CODE_ARRAY):
            return StringPrinter(val, name)
        return None
    if value_type.code == gdb.TYPE_CODE_PTR:
        target = value_type.target().strip_typedefs()
        if target.code == gdb.TYPE_CODE_ARRAY and target.sizeof == 0:
            return dynamic_array(val)
        return None
    if value_type.code == gdb.TYPE_CODE_ARRAY and value_type.sizeof == 0 and val.type.name is None:
        return open_array(val)
    return None


def register_printers(objfile):
    objfile.pretty_printers[:] = [printer for printer in objfile.pretty_printers if getattr(printer, "__name__", None) != lookup.__name__
                                  or getattr(printer, "__module__", None) != __name__]
    objfile.pretty_printers.append(lookup)
