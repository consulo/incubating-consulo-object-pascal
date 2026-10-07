package com.siberika.idea.pascal.lang;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.moveLeftRight.MoveElementLeftRightHandler;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

@ExtensionImpl
public class PascalLeftRightMover implements MoveElementLeftRightHandler {
    @NotNull
    @Override
    public PsiElement[] getMovableSubElements(@NotNull PsiElement element) {
        if (element instanceof PasFormalParameter) {
            return ((PasFormalParameter) element).getNamedIdentList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasFormalParameterSection) {
            return ((PasFormalParameterSection) element).getFormalParameterList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasArgumentList) {
            return ((PasArgumentList) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasEnumType) {
            return ((PasEnumType) element).getNamedIdentDeclList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasConstrainedTypeParam) {
            return ((PasConstrainedTypeParam) element).getNamedIdentList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasClassParent) {
            return ((PasClassParent) element).getTypeIDList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasUsesClause) {
            return ((PasUsesClause) element).getNamespaceIdentList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasClassField) {
            return ((PasClassField) element).getNamedIdentDeclList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasVarDeclaration) {
            return ((PasVarDeclaration) element).getNamedIdentDeclList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasCaseItem) {
            return ((PasCaseItem) element).getConstExpressionOrdList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasSumExpr) {
            return ((PasSumExpr) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasRelationalExpr) {
            return ((PasRelationalExpr) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasProductExpr) {
            return ((PasProductExpr) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasIndexList) {
            return ((PasIndexList) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasCompoundStatement) {
            return ((PasCompoundStatement) element).getStatementList().toArray(PsiElement.EMPTY_ARRAY);
        }
        return PsiElement.EMPTY_ARRAY;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
