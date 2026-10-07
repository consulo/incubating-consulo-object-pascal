package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
@ExtensionImpl
public class PascalSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    private SyntaxHighlighter myValue;

    @NotNull
    public final SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
        if (myValue == null) {
            myValue = new PascalSyntaxHighlighter(project, virtualFile);
        }
        return myValue;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }

}