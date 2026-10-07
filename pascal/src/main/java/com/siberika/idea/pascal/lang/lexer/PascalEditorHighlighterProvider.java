package com.siberika.idea.pascal.lang.lexer;

import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.EditorHighlighter;
import consulo.colorScheme.EditorColorsScheme;
import consulo.language.editor.highlight.EditorHighlighterProvider;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.language.editor.scratch.ScratchUtil;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 28/08/2013
 */
@ExtensionImpl
public class PascalEditorHighlighterProvider implements EditorHighlighterProvider {
    @Override
    public EditorHighlighter getEditorHighlighter(@Nullable Project project, @NotNull FileType fileType, @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors) {
        if ((fileType == PascalFileType.INSTANCE) || (fileType == PPUFileType.INSTANCE) || (fileType == DCUFileType.INSTANCE)
                || isPascalScratchFile(virtualFile)) {
            SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, project, virtualFile);
            if (syntaxHighlighter != null) {
                return new PascalLexerEditorHighlighter(syntaxHighlighter, colors, project, virtualFile);
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public FileType getFileType() {
        return PascalFileType.INSTANCE;
    }

    private boolean isPascalScratchFile(VirtualFile virtualFile) {
        return ScratchUtil.isScratch(virtualFile);
    }
}
