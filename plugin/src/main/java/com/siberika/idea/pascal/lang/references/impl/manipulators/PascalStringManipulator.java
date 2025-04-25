package com.siberika.idea.pascal.lang.references.impl.manipulators;

import com.siberika.idea.pascal.lang.psi.impl.PascalStringImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 21/01/2016
 */
@ExtensionImpl
public class PascalStringManipulator extends AbstractElementManipulator<PascalStringImpl> {
    @Override
    public PascalStringImpl handleContentChange(@NotNull PascalStringImpl psi, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        final String oldText = psi.getText();
        final String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
        return psi.updateText(newText);
    }

    @NotNull
    @Override
    public TextRange getRangeInElement(@NotNull final PascalStringImpl element) {
        return getStringTokenRange(element);
    }

    @Nonnull
    @Override
    public Class<PascalStringImpl> getElementClass() {
        return PascalStringImpl.class;
    }

    private static TextRange getStringTokenRange(final PascalStringImpl element) {
        int textLength = element.getTextLength();
        return textLength >= 2 ? TextRange.from(1, textLength -2) : TextRange.EMPTY_RANGE;
    }
}
