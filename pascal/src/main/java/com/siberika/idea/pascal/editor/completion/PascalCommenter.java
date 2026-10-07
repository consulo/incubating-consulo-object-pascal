package com.siberika.idea.pascal.editor.completion;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
@ExtensionImpl
public class PascalCommenter implements Commenter {
    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return "//";
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return "{";
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return "}";
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return "(*";
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return "*)";
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
