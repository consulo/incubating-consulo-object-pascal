package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.DCUFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.file.FileViewProvider;
import consulo.language.file.VirtualFileViewProviderFactory;
import consulo.language.psi.PsiManager;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-04-25
 */
@ExtensionImpl
public class DCUFileViewProviderFactory implements VirtualFileViewProviderFactory {
    @Nonnull
    @Override
    public FileType getFileType() {
        return DCUFileType.INSTANCE;
    }

    @Override
    public FileViewProvider createFileViewProvider(@Nonnull VirtualFile virtualFile, Language language, @Nonnull PsiManager psiManager, boolean physical) {
        return new PPUViewProvider(psiManager, virtualFile, physical);
    }
}
