package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.version.LanguageVersionUtil;
import consulo.project.Project;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public class PasElementFactory {
    private PasElementFactory() {
    }

    public static PsiElement createLeafFromText(Project project, String text) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("$$.pas", PascalLanguage.INSTANCE, text);
        return PsiTreeUtil.getDeepestFirst(file);
    }

    public static PsiElement createElementFromText(Project project, String text) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("$$.pas", PascalLanguage.INSTANCE, text);
        PsiElement res = file.getFirstChild();
        if (res instanceof PsiErrorElement) {
            res = res.getNextSibling();
        }
        return res;
    }

    public static PsiElement createReplacementElement(PsiElement element, String text) {
        if (element instanceof PascalPsiElement) {
            PsiFileFactory factory = PsiFileFactory.getInstance(element.getProject());
            return factory.createElementFromText(text, element.getLanguage(), LanguageVersionUtil.findDefaultVersion(element.getLanguage()), element.getNode().getElementType(), element);
        } else {
            return createLeafFromText(element.getProject(), text);
        }
    }

    public static <T extends PsiElement> T createElementFromText(Project project, String text, Class<T> aClass) {
        PsiElement file = createElementFromText(project, text);
        return PsiTreeUtil.findChildOfType(file, aClass);

    }
}
