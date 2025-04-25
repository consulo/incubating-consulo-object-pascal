package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
@ExtensionImpl
public class PascalFindUsagesProvider implements FindUsagesProvider {

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return (psiElement instanceof PsiNamedElement) || (PascalReferenceContributor.COMMENT_REFERENCE_TOKENS.contains(psiElement.getNode().getElementType()));
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof PascalNamedElement) {
            return ((PascalNamedElement) element).getType().name().toLowerCase();
        } else {
            return "identifier";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof PasEntityScope) {
            return ((PasEntityScope) element).getName();
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof PasGenericTypeIdent) {
            return ((PasGenericTypeIdent) element).getName() + " = ";
        } else {
            return "";
        }
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
