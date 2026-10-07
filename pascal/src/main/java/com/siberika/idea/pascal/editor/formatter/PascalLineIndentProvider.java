package com.siberika.idea.pascal.editor.formatter;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.codeStyle.lineIndent.JavaLikeLangLineIndentProvider;
import consulo.language.codeStyle.lineIndent.SemanticEditorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import static consulo.language.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.*;

@ExtensionImpl
public class PascalLineIndentProvider extends JavaLikeLangLineIndentProvider {

    private final static HashMap<IElementType, SemanticEditorPosition.SyntaxElement> SYNTAX_MAP = new HashMap<>();
    static {
        SYNTAX_MAP.put(TokenType.WHITE_SPACE, Whitespace);
        SYNTAX_MAP.put(PasTypes.SEMI, Semicolon);
        SYNTAX_MAP.put(PasTypes.BEGIN, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.REPEAT, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.END, BlockClosingBrace);
        SYNTAX_MAP.put(PasTypes.UNTIL, BlockClosingBrace);
        SYNTAX_MAP.put(PasTypes.LBRACK, ArrayOpeningBracket);
        SYNTAX_MAP.put(PasTypes.RBRACK, ArrayClosingBracket);
        SYNTAX_MAP.put(PasTypes.RPAREN, RightParenthesis);
        SYNTAX_MAP.put(PasTypes.LPAREN, LeftParenthesis);
        SYNTAX_MAP.put(PasTypes.COLON, Colon);
        SYNTAX_MAP.put(PasTypes.IF, IfKeyword);
        SYNTAX_MAP.put(PasTypes.WHILE, IfKeyword);
        SYNTAX_MAP.put(PasTypes.ELSE, ElseKeyword);
        SYNTAX_MAP.put(PasTypes.FOR, ForKeyword);
        SYNTAX_MAP.put(PasTypes.DO, DoKeyword);
        SYNTAX_MAP.put(PasTypes.COMMENT, BlockComment);
        SYNTAX_MAP.put(PasTypes.COMMA, Comma);
        SYNTAX_MAP.put(PasTypes.TRY, TryKeyword);

        SYNTAX_MAP.put(PasTypes.TYPE, DoKeyword);
        SYNTAX_MAP.put(PasTypes.VAR, DoKeyword);
        SYNTAX_MAP.put(PasTypes.CONST, DoKeyword);
    }

    @Nullable
    @Override
    protected SemanticEditorPosition.SyntaxElement mapType(@NotNull IElementType tokenType) {
        SYNTAX_MAP.put(PasTypes.TRY, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.EXCEPT, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.FINALLY, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.OF, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.RECORD, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.CLASS, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.OBJECT, BlockOpeningBrace);
        return SYNTAX_MAP.get(tokenType);
    }
  
    @Override
    public boolean isSuitableForLanguage(@NotNull Language language) {
        return language instanceof PascalLanguage;
    }
}
