package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.PPUFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
@ExtensionImpl
public class PPUFileDecompiler extends PascalUnitDecompiler {

    @Nonnull
    @Override
    public FileType getFileType() {
        return PPUFileType.INSTANCE;
    }

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        return doDecompile(file);
    }

    @Override
    PascalCachingUnitDecompiler createCache(Module module, Sdk sdk) {
        return new PPUDecompilerCache(module, sdk);
    }

}
