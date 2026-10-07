package com.siberika.idea.pascal.lang.inspection;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.inspection.InspectionSuppressor;
import consulo.language.editor.inspection.SuppressQuickFix;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class PascalInspectionSuppressor implements InspectionSuppressor {
    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!(element instanceof PascalNamedElement)) {
            return false;
        }
        PsiElement prev = PsiTreeUtil.prevVisibleLeaf(element);
        return (prev instanceof PsiComment) && "{!}".equals(prev.getText());
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
