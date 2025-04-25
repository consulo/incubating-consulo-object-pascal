package com.siberika.idea.pascal.lang.inspection;

import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

import static com.siberika.idea.pascal.PascalBundle.message;

@ExtensionImpl
public class DestructorInheritedInspection extends PascalLocalInspectionBase {

    @Override
    public void checkRoutine(PascalRoutine routine, ProblemsHolder holder, boolean isOnTheFly) {
        if (routine instanceof PasRoutineImplDecl) {
            if (RoutineUtil.isDestructor(routine)) {
                PasCompoundStatement code = RoutineUtil.retrieveRoutineCodeBlock(routine);
                if (code != null) {
                    PsiElement inherited = PsiUtil.findChildByElementType(code, PasTypes.INHERITED);
                    if (null == inherited) {
                        PsiElement end = code.getLastChild();
                        holder.registerProblem(holder.getManager().createProblemDescriptor(end, message("inspection.warn.destructor.no.inherited"), true,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly,
                                new IdentQuickFixes.AddInheritedAction()));
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "No inherited destructor call detection";
    }
}
