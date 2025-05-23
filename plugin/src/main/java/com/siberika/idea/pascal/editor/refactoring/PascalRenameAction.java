package com.siberika.idea.pascal.editor.refactoring;

import com.siberika.idea.pascal.PascalBundle;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.refactoring.RefactoringFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 27/04/2018
 */
public class PascalRenameAction extends BaseIntentionAction {
    private final PsiElement element;
    private final String newName;
    private final String name;

    public PascalRenameAction(PsiElement element, String newName, String name) {
        super();
        this.element = element;
        this.newName = newName;
        this.name = name;
    }

    @NotNull
    @Override
    public String getText() {
        return name;
    }

    @NotNull
    public String getFamilyName() {
        return PascalBundle.message("action.familyName");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        RefactoringFactory.getInstance(project).createRename(element, newName, false, false).run();
    }

}
