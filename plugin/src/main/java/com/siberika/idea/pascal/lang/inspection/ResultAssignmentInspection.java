package com.siberika.idea.pascal.lang.inspection;

import com.siberika.idea.pascal.editor.highlighter.PascalReadWriteAccessDetector;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import static com.siberika.idea.pascal.PascalBundle.message;

public class ResultAssignmentInspection extends PascalLocalInspectionBase {
    @Override
    public void checkRoutine(PascalRoutine routine, ProblemsHolder holder, boolean isOnTheFly) {
        if (routine instanceof PasRoutineImplDecl) {
            if (routine.isFunction()) {
                PasCompoundStatement code = RoutineUtil.retrieveRoutineCodeBlock(routine);
                if (code != null) {
                    Collection<PascalPsiElement> elements =
                        PsiTreeUtil.findChildrenOfAnyType(code, PasFullyQualifiedIdent.class, PasExitStatement.class);
                    for (PascalPsiElement element : elements) {
                        if (element instanceof PasFullyQualifiedIdent) {
                            List<PasSubIdent> subidents = ((PasFullyQualifiedIdent) element).getSubIdentList();
                            if (!subidents.isEmpty() && RoutineUtil.isFunctionResultReference(subidents.get(0), routine.getName())) {
                                if (PascalReadWriteAccessDetector.isWriteAccess(element)) {
                                    return;
                                }
                            }
                        }
                        else if (PsiTreeUtil.getChildOfType(element, PasExpression.class) != null) {
                            // exit statement with result expression
                            return;
                        }
                        else {
                            // exit statement without result expression
                            addWarning(holder, isOnTheFly, element);
                            return;
                        }
                    }
                    PsiElement end = code.getLastChild();
                    addWarning(holder, isOnTheFly, end);
                }
            }
        }
    }

    private void addWarning(ProblemsHolder holder, boolean isOnTheFly, PsiElement element) {
        holder.registerProblem(holder.getManager().createProblemDescriptor(
            element,
            message("inspection.warn.function.no.result.assignment"),
            true,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            isOnTheFly,
            new IdentQuickFixes.AddResultAssignmentAction()
        ));
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("No result assignment detection");
    }
}
