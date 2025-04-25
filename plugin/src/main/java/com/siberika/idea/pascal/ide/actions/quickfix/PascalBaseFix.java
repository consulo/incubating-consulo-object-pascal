package com.siberika.idea.pascal.ide.actions.quickfix;

import consulo.language.editor.inspection.LocalQuickFix;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class PascalBaseFix implements LocalQuickFix {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Pascal";
    }
}
