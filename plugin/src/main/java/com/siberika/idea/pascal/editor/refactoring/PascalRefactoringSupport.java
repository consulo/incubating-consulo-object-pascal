package com.siberika.idea.pascal.editor.refactoring;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.refactoring.RefactoringSupportProvider;
import consulo.language.editor.refactoring.action.RefactoringActionHandler;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class PascalRefactoringSupport extends RefactoringSupportProvider {
    @Override
    public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        PsiElement parent = element.getParent();
        if (parent instanceof PasFormalParameter) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, @Nullable PsiElement context) {
        PsiElement parent = element.getParent();
        if (parent instanceof PasFormalParameter) {
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public RefactoringActionHandler getIntroduceVariableHandler() {
        return new PascalIntroduceVariableHandler();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
