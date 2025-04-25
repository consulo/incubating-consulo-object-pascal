package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static consulo.colorScheme.TextAttributesKey.createTextAttributesKey;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalSyntaxHighlighterBase extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORDS = createTextAttributesKey("Pascal keyword", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBERS = createTextAttributesKey("Pascal number", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING = createTextAttributesKey("Pascal string", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT = createTextAttributesKey("Pascal comment", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OPERATORS = createTextAttributesKey("Pascal operation", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey SEMICOLON = createTextAttributesKey("Pascal semicolon", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey PARENTHESES = createTextAttributesKey("Pascal parentheses", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey SYMBOLS = createTextAttributesKey("Pascal symbol", DefaultLanguageHighlighterColors.COMMA);

    private final Map<IElementType, TextAttributesKey> colors = new HashMap<IElementType, TextAttributesKey>();

    @SuppressWarnings("deprecation")
    public PascalSyntaxHighlighterBase() {
        colors.put(PascalLexer.STRING_LITERAL, STRING);
        colors.put(PascalLexer.COMMENT, COMMENT);
        colors.put(PascalLexer.SEMI, SEMICOLON);
        colors.put(TokenType.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
        fillMap(colors, PascalLexer.NUMBERS, NUMBERS);           // TODO: change to safeMap when it will be supported by ultimate edition
        fillMap(colors, PascalLexer.KEYWORDS, KEYWORDS);
        fillMap(colors, PascalLexer.OPERATORS, OPERATORS);
        fillMap(colors, PascalLexer.PARENS, PARENTHESES);
        fillMap(colors, PascalLexer.SYMBOLS, SYMBOLS);
    }

    // Used in decompilers
    @NotNull
    public Lexer getHighlightingLexer() {
        return new PascalLexer.SyntaxHighlightingPascalLexer(null, null);
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(colors.get(tokenType));
    }

}
