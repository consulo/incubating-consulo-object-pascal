package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalPsiElementImpl extends ASTWrapperPsiElement implements PascalPsiElement {
    public PascalPsiElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return getNode().getElementType().toString();
    }
}

