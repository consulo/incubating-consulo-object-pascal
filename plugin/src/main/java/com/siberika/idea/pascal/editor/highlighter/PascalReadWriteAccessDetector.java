package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.highlight.ReadWriteAccessDetector;
import consulo.language.editor.parameterInfo.ParameterInfoUtils;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@ExtensionImpl
public class PascalReadWriteAccessDetector extends ReadWriteAccessDetector {

    public static final Logger LOG = Logger.getInstance(PascalReadWriteAccessDetector.class);

    @Override
    public boolean isReadWriteAccessible(@NotNull PsiElement element) {
        if (element instanceof PascalIdentDecl) {
            return ((PascalIdentDecl) element).getAccess() == PasField.Access.READWRITE;
        } else if ((element instanceof PascalNamedElement) && (
                PsiUtil.isFormalParameterName((PascalNamedElement) element) || element.getParent() instanceof PascalInlineDeclaration)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isDeclarationWriteAccess(@NotNull PsiElement element) {
        if (element instanceof PascalIdentDecl) {
            return ((PascalIdentDecl) element).getValue() != null;
        } else if (element instanceof PascalNamedElement) {
            PsiElement parent = element.getParent();
            if (PsiUtil.isFormalParameterName((PascalNamedElement) element)) {
                PasFormalParameter pasFormalParameter = (PasFormalParameter) parent;
                return pasFormalParameter.getConstExpression() != null;
            } else if (parent instanceof PasInlineVarDeclaration) {
                PsiElement next = PsiTreeUtil.skipSiblingsForward(parent, PsiUtil.ELEMENT_WS_COMMENTS);
                return next instanceof PasAssignPart;
            } else if (parent instanceof PasInlineConstDeclaration || parent instanceof PasForInlineDeclaration) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public Access getReferenceAccess(@NotNull PsiElement referencedElement, @NotNull PsiReference reference) {
        return isWriteAccess(reference.getElement()) ? Access.Write : Access.Read;
    }

    public static boolean isWriteAccess(PsiElement element) {
        if (element instanceof PascalNamedElement) {
            if (ContextUtil.isAssignLeftPart(element)) {
                return true;
            } else {
                if (element.getParent() instanceof PascalQualifiedIdent) {
                    element = element.getParent();
                }
                PasCallExpr callExpr = RoutineUtil.retrieveCallExpr(element);
                if (callExpr != null) {
                    return isWriteModifier(retrieveActualParamAccess(callExpr, (PascalNamedElement) element));
                }
                PsiElement next = PsiTreeUtil.skipSiblingsForward(element.getParent(), PsiUtil.ELEMENT_WS_COMMENTS);
                return (next != null) && (next.getNode().getElementType() == PasTypes.ASSIGN);
            }
        } else {
            return false;
        }
    }

    private static boolean isWriteModifier(ParamModifier modifier) {
        return modifier == ParamModifier.VAR || modifier == ParamModifier.OUT;
    }

    private static ParamModifier retrieveActualParamAccess(PasCallExpr callExpr, PascalNamedElement element) {
        Collection<PascalRoutineEntity> routines = PasReferenceUtil.resolveRoutines(callExpr);
        int paramIndex = ParameterInfoUtils.getCurrentParameterIndex(callExpr.getArgumentList().getNode(), element.getTextRange().getStartOffset(), PasTypes.COMMA);
        PascalRoutineEntity first = null;
        for (PascalRoutineEntity routine : routines) {
            first = first != null ? first : routine;
            if (RoutineUtil.isSuitable(callExpr, routine)) {
                ParamModifier res = getFormalParamModifier(routine, paramIndex);
                if (res != null) {
                    return res;
                }
            }
        }
        if (first != null) {
            return getFormalParamModifier(first, paramIndex);
        }
        return ParamModifier.NONE;
    }

    private static ParamModifier getFormalParamModifier(PascalRoutineEntity routine, int paramIndex) {
        List<ParamModifier> modifierList = routine.getFormalParameterAccess();
        if ((paramIndex >= 0) && (paramIndex < modifierList.size())) {
            return modifierList.get(paramIndex);
        }
        return null;
    }

    @NotNull
    @Override
    public Access getExpressionAccess(@NotNull PsiElement expression) {
        return Access.Read;
    }
}
