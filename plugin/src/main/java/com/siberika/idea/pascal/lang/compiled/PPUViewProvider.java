package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PascalLanguage;
import consulo.ide.ServiceManager;
import consulo.language.Language;
import consulo.language.content.FileIndexFacade;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.file.SingleRootFileViewProvider;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReference;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 14/11/2013
 */
public class PPUViewProvider extends SingleRootFileViewProvider implements FileViewProvider {
    public PPUViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean physical) {
        super(manager, virtualFile, physical);
    }

    // WTF in ascending method?
    @Override
    public PsiReference findReferenceAt(final int offset) {
        return findReferenceAt(offset, PascalLanguage.INSTANCE);
    }

    @Override
    protected PsiFile createFile(@NotNull final Project project, @NotNull final VirtualFile vFile, @NotNull final FileType fileType) {
        final FileIndexFacade fileIndex = ServiceManager.getService(project, FileIndexFacade.class);
        if (fileIndex.isInLibraryClasses(vFile) || !fileIndex.isInSource(vFile)) {
            if (fileType instanceof DCUFileType) {
                return new DCUFileImpl(getManager(), this);
            } else {
                return new PPUFileImpl(getManager(), this);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Language getBaseLanguage() {
        return PascalLanguage.INSTANCE;
    }

}
