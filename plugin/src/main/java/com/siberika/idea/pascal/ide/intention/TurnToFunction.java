package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

class TurnToFunction extends RoutineIntention {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return ObjectPascalLocalize.actionFixRoutineToFunction();
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        return (routine != null) && !routine.isFunction();
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
        PascalRoutine routine = getRoutineHeader(editor, element);
        if ((null != routine) && !routine.isFunction()) {
            PascalRoutine target = getTargetRoutine(editor, element);
            changeToFunction(project, editor, routine, true);
            if (target != null && !target.isFunction()) {
                changeToFunction(project, editor, target, false);
            }
        }
    }

    private void changeToFunction(Project project, Editor editor, PascalRoutine routine, boolean moveCaret) {
        PasFormalParameterSection params = routine.getFormalParameterSection();
        PsiElement anchor = params != null ? params : PsiTreeUtil.getChildOfType(routine, PascalNamedElement.class);
        if (anchor != null) {
            PsiElement added = routine.addAfter(PasElementFactory.createLeafFromText(project, ":"), anchor);
            if ((added != null) && moveCaret) {
                editor.getCaretModel().moveToOffset(added.getTextOffset() + 1);
            }
        }
        switchKeyword(project, routine, PasTypes.PROCEDURE, "function");
    }

}
