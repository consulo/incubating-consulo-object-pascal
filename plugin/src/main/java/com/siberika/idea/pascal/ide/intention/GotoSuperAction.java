package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.util.EditorUtil;
import consulo.codeEditor.Editor;
import consulo.language.Language;
import consulo.language.editor.action.GotoSuperActionHander;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

class GotoSuperAction extends NavIntentionActionBase  {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.struct.goto.super");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return GotoSuper.hasSuperTargets(element);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        Collection<PasEntityScope> targets = GotoSuper.search(element).findAll();
        EditorUtil.navigateTo(editor, getText(), targets);
    }
}
