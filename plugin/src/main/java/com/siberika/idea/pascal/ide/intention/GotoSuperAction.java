package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.util.EditorUtil;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

import java.util.Collection;

class GotoSuperAction extends NavIntentionActionBase  {

    @Nonnull
    @Override
    public LocalizeValue getText() {
        return ObjectPascalLocalize.actionFixStructGotoSuper();
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
        return GotoSuper.hasSuperTargets(element);
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
        Collection<PasEntityScope> targets = GotoSuper.search(element).findAll();
        EditorUtil.navigateTo(editor, getText(), targets);
    }
}
