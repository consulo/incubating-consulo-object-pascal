package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import consulo.language.ast.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PasFormalParameterMixin extends PascalPsiElementImpl implements PasFormalParameter {
    public PasFormalParameterMixin(ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public List<? extends PascalNamedElement> getNamedIdentDeclList() {
        return getNamedIdentList();
    }

}
