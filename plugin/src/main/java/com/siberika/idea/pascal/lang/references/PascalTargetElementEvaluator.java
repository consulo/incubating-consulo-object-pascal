package com.siberika.idea.pascal.lang.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import consulo.codeInsight.TargetElementUtilEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalTargetElementEvaluator extends TargetElementUtilEx.Adapter {
    @Nullable
    @Override
    public PsiElement getGotoDeclarationTarget(@NotNull PsiElement element, @Nullable PsiElement navElement) {
        if (element instanceof PasExportedRoutine) {
            return SectionToggle.retrieveImplementation((PascalRoutine) element, true);
        }
        return super.getGotoDeclarationTarget(element, navElement);
    }
}
