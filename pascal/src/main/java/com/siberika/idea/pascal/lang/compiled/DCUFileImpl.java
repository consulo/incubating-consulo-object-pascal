package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.DCUFileType;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiManager;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
public class DCUFileImpl extends CompiledFileImpl {
    public DCUFileImpl(PsiManager myManager, FileViewProvider provider) {
        super(myManager, provider);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return DCUFileType.INSTANCE;
    }

}
