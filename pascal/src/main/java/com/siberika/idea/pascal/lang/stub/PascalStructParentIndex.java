package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;

import java.util.Locale;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class PascalStructParentIndex extends StringStubIndexExtension<PascalStructType> {
    public static final StubIndexKey<String, PascalStructType> KEY = StubIndexKey.createIndexKey("pascal.struct.parent");

    @Override
    public StubIndexKey<String, PascalStructType> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return PascalFileElementType.getStubIndexVersion();
    }

    public static String parentKey(String name) {
        String cleaned = ResolveUtil.cleanupName(name);
        int generic = cleaned.indexOf('<');
        if (generic >= 0) {
            cleaned = cleaned.substring(0, generic);
        }
        cleaned = cleaned.trim();
        int space = cleaned.lastIndexOf(' ');
        if (space >= 0) {
            cleaned = cleaned.substring(space + 1);
        }
        return cleaned.substring(cleaned.lastIndexOf('.') + 1).toUpperCase(Locale.ROOT);
    }
}
