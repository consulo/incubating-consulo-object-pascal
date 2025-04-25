package com.siberika.idea.pascal.lang.lexer;

import consulo.colorScheme.EditorColorsScheme;
import consulo.document.event.DocumentEvent;
import consulo.language.editor.highlight.LexerEditorHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.lexer.FlexLexer;
import consulo.language.lexer.Lexer;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 28/08/2013
 */
public class PascalLexerEditorHighlighter extends LexerEditorHighlighter {

    @Nullable
    private Project project;
    @Nullable
    private VirtualFile virtualFile;

    public PascalLexerEditorHighlighter(@NotNull SyntaxHighlighter highlighter, @NotNull EditorColorsScheme scheme, @Nullable Project project, @Nullable VirtualFile virtualFile) {
        super(highlighter, scheme);
        this.project = project;
        this.virtualFile = virtualFile;
        initPascalFlexLexer();
    }

    @Override
    public synchronized void documentChanged(DocumentEvent e) {
        super.documentChanged(e);
        if (getDocument() != null) {
            initPascalFlexLexer();
        }
    }

    private void initPascalFlexLexer() {
        Lexer lexer = getLexer();
        if (lexer instanceof PascalLexer) {
            FlexLexer flexLexer = ((PascalLexer) lexer).getFlexLexer();
            if (flexLexer instanceof PascalFlexLexerImpl) {
                PascalFlexLexerImpl pascalFlexLexer = (PascalFlexLexerImpl) flexLexer;
                pascalFlexLexer.setProject(project);
                pascalFlexLexer.setVirtualFile(virtualFile);
            }
        }
    }
}
