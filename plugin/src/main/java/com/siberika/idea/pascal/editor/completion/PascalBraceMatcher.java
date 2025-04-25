package com.siberika.idea.pascal.editor.completion;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.HighlighterIterator;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.highlight.LanguageBraceMatcher;
import consulo.language.psi.PsiFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
@ExtensionImpl
public class PascalBraceMatcher implements LanguageBraceMatcher {
    @Override
    public int getBraceTokenGroupId(IElementType tokenType) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static final TokenSet END_PAIRS = TokenSet.create(PasTypes.BEGIN, PasTypes.TRY, PasTypes.CASE, PasTypes.ASM,
            PasTypes.CLASS, PasTypes.INTERFACE, PasTypes.DISPINTERFACE, PasTypes.OBJECT, PasTypes.RECORD);

    @Override
    public boolean isLBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        return END_PAIRS.contains((IElementType) iterator.getTokenType()) || (iterator.getTokenType() == PasTypes.REPEAT)
            || (iterator.getTokenType() == PasTypes.LBRACK) || (iterator.getTokenType() == PasTypes.LPAREN);
    }

    @Override
    public boolean isRBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        return (iterator.getTokenType() == PasTypes.END) || (iterator.getTokenType() == PasTypes.UNTIL)
            || (iterator.getTokenType() == PasTypes.RBRACK) || (iterator.getTokenType() == PasTypes.RPAREN);
    }

    @Override
    public boolean isPairBraces(IElementType tokenType, IElementType tokenType2) {
        return isPairBracesInternal(tokenType, tokenType2) || isPairBracesInternal(tokenType2, tokenType);
    }

    private boolean isPairBracesInternal(IElementType tokenType, IElementType tokenType2) {
        return (END_PAIRS.contains(tokenType) && (tokenType2 == PasTypes.END))
            || ((tokenType == PasTypes.REPEAT) && (tokenType2 == PasTypes.UNTIL))
            || ((tokenType == PasTypes.LBRACK) && (tokenType2 == PasTypes.RBRACK))
            || ((tokenType == PasTypes.LPAREN) && (tokenType2 == PasTypes.RPAREN));
    }

    @Override
    public boolean isStructuralBrace(HighlighterIterator iterator, CharSequence text, FileType fileType) {
        return END_PAIRS.contains((IElementType) iterator.getTokenType()) || (iterator.getTokenType() == PasTypes.END)
                || (iterator.getTokenType() == PasTypes.REPEAT) || (iterator.getTokenType() == PasTypes.UNTIL);
    }

    @Nullable
    @Override
    public IElementType getOppositeBraceTokenType(@NotNull IElementType type) {
        if (END_PAIRS.contains(type)) { return PasTypes.END; }
        if (type == PasTypes.END)    { return PasTypes.BEGIN; }
        if (type == PasTypes.LBRACK) { return PasTypes.RBRACK; }
        if (type == PasTypes.RBRACK) { return PasTypes.LBRACK; }
        if (type == PasTypes.LPAREN) { return PasTypes.RPAREN; }
        if (type == PasTypes.RPAREN) { return PasTypes.LPAREN; }
        if (type == PasTypes.REPEAT) { return PasTypes.UNTIL; }
        if (type == PasTypes.UNTIL) { return PasTypes.REPEAT; }
        return null;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return false;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
