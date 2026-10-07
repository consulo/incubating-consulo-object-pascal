package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.PPUFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-04-25
 */
@ExtensionImpl
public class PPUCompiledStubBuilder extends PascalCompiledStubBuilder {
    @Nonnull
    @Override
    public FileType getFileType() {
        return PPUFileType.INSTANCE;
    }
}
