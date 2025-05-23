package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

class TurnToProcedure extends RoutineIntention {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.routine.to.procedure");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    public String getFamilyName() {
        return PascalBundle.message("action.fix.routine.to.procedure.family");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        return (routine != null) && routine.isFunction();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PascalRoutine routine = getRoutineHeader(editor, element);
        if ((null != routine) && routine.isFunction()) {
            PascalRoutine target = getTargetRoutine(editor, element);
            changeToProcedure(project, routine);
            if (target != null && target.isFunction()) {
                changeToProcedure(project, target);
            }
        }
    }

}
