package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import consulo.language.lexer.Lexer;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalSyntaxHighlighter extends PascalSyntaxHighlighterBase {

    final Project project;
    final VirtualFile virtualFile;

    @SuppressWarnings("deprecation")
    public PascalSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
        super();
        this.project = project;
        this.virtualFile = virtualFile;
    }

    @Override
    @NotNull
    public Lexer getHighlightingLexer() {
        return new PascalLexer.SyntaxHighlightingPascalLexer(project, virtualFile);
    }
}
