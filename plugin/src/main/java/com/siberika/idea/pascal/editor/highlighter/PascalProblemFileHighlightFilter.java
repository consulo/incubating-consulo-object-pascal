package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.PascalFileType;
import consulo.util.lang.function.Condition;
import consulo.virtualFileSystem.VirtualFile;

public class PascalProblemFileHighlightFilter implements Condition<VirtualFile> {
    @Override
    public boolean value(VirtualFile virtualFile) {
        return virtualFile.getFileType() == PascalFileType.INSTANCE;
    }
}
