package com.siberika.idea.pascal.lang.context;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.object.pascal.psi.PasBaseReferenceExpr;

public class ContextUtil {
    public static boolean isFieldDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasClassField);
    }

    public static boolean isPropertyDecl(PascalNamedElement entityDecl) {
        return entityDecl.getParent() instanceof PasClassProperty;
    }

    /**
     * Checks if the entityDecl is a declaration of variable or formal parameter
     *
     * @param entityDecl entity declaration to check
     * @return true if the entityDecl is a declaration of variable or formal parameter
     */
    public static boolean isVariableDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PascalVariableDeclaration);
    }

    // Checks if the entityDecl is a declaration of constant
    public static boolean isConstDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasConstDeclaration);
    }

    // Checks if the entityDecl is a declaration of an enumeration constant
    public static boolean isEnumDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasEnumType);
    }

    public static boolean belongsToInterface(PsiElement ident) {
        return PsiTreeUtil.getParentOfType(ident, PasUnitInterface.class) != null;
    }

    // Check if the named element is the left part of an assignment statement including assignment in FOR statement
    public static boolean isAssignLeftPart(PsiElement element) {
        PsiElement expr = PsiUtil.skipToExpression(element);
        if (expr instanceof PasBaseReferenceExpr) {
            PsiElement parent = expr.getParent();
            parent = parent instanceof PasExpression ? parent : PsiTreeUtil.skipParentsOfType(expr, PasUnaryExpr.class, PasParenExpr.class, PasDereferenceExpr.class, PasIndexExpr.class);
            if (parent instanceof PasExpression) {
                return PsiTreeUtil.skipSiblingsForward(parent, PsiUtil.ELEMENT_WS_COMMENTS) instanceof PasAssignPart;
            }
        } else if (expr instanceof PasForStatement) {
            return true;
        }
        return false;
    }

    // Check if the named element is the right part of an assignment statement
    public static boolean isAssignRightPart(PascalNamedElement element) {
        PsiElement expr = PsiUtil.skipToExpression(element);
        if (expr instanceof PasBaseReferenceExpr) {
            PsiElement parent = expr.getParent();
            parent = parent instanceof PasExpression ? parent.getParent() : null;
            return parent instanceof PasAssignPart;
        }
        return false;
    }

    public static boolean isPropertyGetter(PasClassPropertySpecifier spec) {
        return "read".equalsIgnoreCase(spec.getFirstChild().getText());
    }

}
