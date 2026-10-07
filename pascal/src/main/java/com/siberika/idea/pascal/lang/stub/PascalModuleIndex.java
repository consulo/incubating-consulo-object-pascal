package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 11/02/2018
 */
@ExtensionImpl
public class PascalModuleIndex extends StringStubIndexExtension {

    public static final StubIndexKey<String, PascalModule> KEY =
            StubIndexKey.createIndexKey("pascal.module");

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
