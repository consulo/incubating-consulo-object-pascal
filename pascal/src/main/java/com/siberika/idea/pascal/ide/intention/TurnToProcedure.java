package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

class TurnToProcedure extends RoutineIntention {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return ObjectPascalLocalize.actionFixRoutineToProcedure();
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        return (routine != null) && routine.isFunction();
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
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
