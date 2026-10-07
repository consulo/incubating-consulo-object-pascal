package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;
import org.jetbrains.annotations.NotNull;

@ExtensionImpl
public class PascalSymbolIndex extends StringStubIndexExtension {

    public static final StubIndexKey<String, PascalNamedElement> KEY =
            StubIndexKey.createIndexKey("pascal.symbol");

    @NotNull
    @Override
    public StubIndexKey getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return PascalFileElementType.getStubIndexVersion();
    }
}
