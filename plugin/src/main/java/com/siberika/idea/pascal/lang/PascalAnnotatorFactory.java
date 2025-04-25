package com.siberika.idea.pascal.lang;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2025-04-25
 */
@ExtensionImpl
public class PascalAnnotatorFactory implements AnnotatorFactory {
    @Nullable
    @Override
    public Annotator createAnnotator() {
        return new PascalAnnotator();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
