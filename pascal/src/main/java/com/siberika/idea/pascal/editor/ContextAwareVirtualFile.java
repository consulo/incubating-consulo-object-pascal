package com.siberika.idea.pascal.editor;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import jakarta.annotation.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 21/04/2017
 */
public class ContextAwareVirtualFile extends LightVirtualFile {

    private final SmartPsiElementPointer<PsiElement> contextElement;

    public ContextAwareVirtualFile(String name, CharSequence content, PsiElement contextElement) {
        super(name, PascalLanguage.INSTANCE, content);
        this.contextElement = SmartPointerManager.createPointer(contextElement);
    }

    @Nullable
    public PsiElement getContextElement() {
        return contextElement.getElement();
    }
}
