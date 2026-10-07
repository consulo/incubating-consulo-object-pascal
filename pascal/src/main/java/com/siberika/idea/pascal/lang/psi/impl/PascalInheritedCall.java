package com.siberika.idea.pascal.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceService;
import org.jetbrains.annotations.NotNull;

public abstract class PascalInheritedCall extends PascalPsiElementImpl {
    public PascalInheritedCall(final ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return PsiReferenceService.getService().getContributedReferences(this);
    }

}
