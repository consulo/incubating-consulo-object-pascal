package com.siberika.idea.pascal;

import consulo.language.Language;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PascalLanguage extends Language {

    public static final PascalLanguage INSTANCE = new PascalLanguage();

    private PascalLanguage() {
        super("Pascal");
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }
}
