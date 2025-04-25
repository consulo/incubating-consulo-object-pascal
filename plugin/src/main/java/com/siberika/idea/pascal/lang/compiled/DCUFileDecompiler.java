package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.DCUFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
@ExtensionImpl
public class DCUFileDecompiler extends PascalUnitDecompiler {

    @Nonnull
    @Override
    public FileType getFileType() {
        return DCUFileType.INSTANCE;
    }

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        return doDecompile(file);
    }

    @Override
    PascalCachingUnitDecompiler createCache(Module module, Sdk sdk) {
        return new DCUCachingDecompiler(sdk);
    }
}
