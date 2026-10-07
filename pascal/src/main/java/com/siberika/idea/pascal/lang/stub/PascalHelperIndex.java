package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;
import org.jetbrains.annotations.NotNull;

@ExtensionImpl
public class PascalHelperIndex extends StringStubIndexExtension {

    public static final StubIndexKey<String, PascalStructType> KEY =
            StubIndexKey.createIndexKey("pascal.helper");

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
