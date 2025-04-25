package com.siberika.idea.pascal.ide.extensions;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.ide.actions.ActionImplement;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.Language;
import consulo.language.editor.action.LanguageCodeInsightActionHandler;
import consulo.language.editor.generation.ImplementMethodHandler;
import consulo.language.editor.refactoring.ContextAwareActionHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.AnAction;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * User: LPUser
 * Date: 03/07/2018
 * Time: 16:35
 */
@ExtensionImpl
public class PascalImplementMethodsHandler implements ContextAwareActionHandler, LanguageCodeInsightActionHandler, ImplementMethodHandler {
    private ActionImplement getActionImplement() {
        AnAction action = ActionManager.getInstance().getAction("Pascal.OverrideMethod");
        if (action == null || !(action instanceof ActionImplement)) {
            return null;
        }
        return (ActionImplement) action;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        ActionImplement actionImplement = getActionImplement();
        if (actionImplement == null) {
            return;
        }

        PsiElement elementAt = file.findElementAt(editor.getCaretModel().getOffset());
        if (elementAt != null) {
            actionImplement.showOverrideDialog(elementAt, editor);
        }
    }

    @Override
    public boolean isAvailableForQuickList(@NotNull Editor editor, @NotNull PsiFile file, @NotNull DataContext dataContext) {
        return true;
    }

    @Override
    public boolean isValidFor(Editor editor, PsiFile file) {
        return true;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
