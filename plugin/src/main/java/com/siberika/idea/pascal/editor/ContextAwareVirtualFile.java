package com.siberika.idea.pascal.editor;

import consulo.language.file.light.LightVirtualFile;
import consulo.language.psi.PsiElement;

/**
 * Author: George Bakhtadze
 * Date: 21/04/2017
 */
public class ContextAwareVirtualFile extends LightVirtualFile {

    private final PsiElement contextElement;

    public ContextAwareVirtualFile(String name, CharSequence content, PsiElement contextElement) {
        super(name, content);
        this.contextElement = contextElement;
    }

    public PsiElement getContextElement() {
        return contextElement;
    }
}
