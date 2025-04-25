package com.siberika.idea.pascal.ide.actions;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.ImplementationTextSelectioner;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 25/02/2017
 */
@ExtensionImpl
public class PascalImplementationTextSelectioner implements ImplementationTextSelectioner {
    @Override
    public int getTextStartOffset(@NotNull PsiElement element) {
        element = findElement(element);
        final TextRange textRange = element.getTextRange();
        return textRange.getStartOffset();
    }

    @Override
    public int getTextEndOffset(@NotNull PsiElement element) {
        element = findElement(element);
        final TextRange textRange = element.getTextRange();
        return textRange.getEndOffset();
    }

    private PsiElement findElement(PsiElement element) {
        if (element instanceof PasExportedRoutine) {
            PsiElement impl = SectionToggle.retrieveImplementation((PascalRoutine) element, true);
            return impl != null ? impl : element;
        } else if ((element instanceof PasGenericTypeIdent) && (element.getParent() instanceof PasTypeDeclaration)) {
            return element.getParent();
        } else if (element.getParent() instanceof PasExportedRoutine) {
            PsiElement impl = SectionToggle.retrieveImplementation((PascalRoutine) element.getParent(), true);
            return impl != null ? impl : element;
        }
        return element;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
