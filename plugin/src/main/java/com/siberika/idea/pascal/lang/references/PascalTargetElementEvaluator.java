package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.TargetElementUtilExtender;
import consulo.language.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class PascalTargetElementEvaluator implements TargetElementUtilExtender {
    @Nullable
    @Override
    public PsiElement getGotoDeclarationTarget(@NotNull PsiElement element, @Nullable PsiElement navElement) {
        if (element instanceof PasExportedRoutine) {
            return SectionToggle.retrieveImplementation((PascalRoutine) element, true);
        }
        return null;
    }
}
