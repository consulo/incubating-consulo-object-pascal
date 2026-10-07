package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.language.ast.IElementType;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalPsiElementType extends IElementType {
    public PascalPsiElementType(String debug) {
        super(debug, PascalLanguage.INSTANCE);
    }
}

