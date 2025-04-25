package com.siberika.idea.pascal.lang.parser;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public interface PascalFile extends PsiFile {
    PsiElement getImplementationSection();
}
