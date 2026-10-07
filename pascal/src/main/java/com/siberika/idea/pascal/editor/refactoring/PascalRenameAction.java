package com.siberika.idea.pascal.editor.refactoring;

import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.refactoring.RefactoringFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

/**
 * @author George Bakhtadze
 * @since 2018-04-27
 */
public class PascalRenameAction extends BaseIntentionAction {
    private final PsiElement element;
    private final String newName;
    @Nonnull
    private final LocalizeValue myName;

    public PascalRenameAction(PsiElement element, String newName, @Nonnull LocalizeValue name) {
        super();
        this.element = element;
        this.newName = newName;
        this.myName = name;
    }

    @Nonnull
    @Override
    public LocalizeValue getText() {
        return myName;
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@Nonnull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        RefactoringFactory.getInstance(project).createRename(element, newName, false, false).run();
    }
}
