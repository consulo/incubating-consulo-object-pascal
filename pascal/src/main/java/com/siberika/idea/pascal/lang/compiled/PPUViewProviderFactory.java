package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.PPUFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.file.FileViewProvider;
import consulo.language.file.VirtualFileViewProviderFactory;
import consulo.language.psi.PsiManager;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 14/11/2013
 */
@ExtensionImpl
public class PPUViewProviderFactory implements VirtualFileViewProviderFactory {
    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean physical) {
        return new PPUViewProvider(manager, file, physical);
    }

    @Nonnull
    @Override
    public FileType getFileType() {
        return PPUFileType.INSTANCE;
    }
}
