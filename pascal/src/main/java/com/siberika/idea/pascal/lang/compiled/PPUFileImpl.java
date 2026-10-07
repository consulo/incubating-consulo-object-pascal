package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.PPUFileType;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiManager;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
public class PPUFileImpl extends CompiledFileImpl {
    public PPUFileImpl(PsiManager myManager, FileViewProvider provider) {
        super(myManager, provider);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PPUFileType.INSTANCE;
    }

}
