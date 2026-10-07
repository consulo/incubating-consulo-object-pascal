package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.lexer.PascalFlexLexer;
import com.siberika.idea.pascal.lang.psi.PasLiteralExpr;
import com.siberika.idea.pascal.lang.psi.PasStringFactor;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.action.JavaLikeQuoteHandler;
import consulo.language.editor.action.SimpleTokenSetQuoteHandler;
import consulo.language.psi.PsiElement;
import consulo.object.pascal.psi.PasBaseReferenceExpr;
import org.jetbrains.annotations.NotNull;

public class PascalQuoteHandler extends SimpleTokenSetQuoteHandler implements JavaLikeQuoteHandler {

    private static final TokenSet STRING_TOKENS = TokenSet.create(PasTypes.STRING_LITERAL);

    PascalQuoteHandler() {
        super(PasTypes.STRING_LITERAL, PascalFlexLexer.STRING_LITERAL_UNC);
    }

    @Override
    public TokenSet getConcatenatableStringTokenTypes() {
        return STRING_TOKENS;
    }

    @Override
    public String getStringConcatenationOperatorRepresentation() {
        return "+";
    }

    @Override
    public TokenSet getStringTokenTypes() {
        return myLiteralTokenSet;
    }

    @Override
    public boolean isAppropriateElementTypeForLiteral(@NotNull IElementType tokenType) {
        return true;
    }

    @Override
    public boolean needParenthesesAroundConcatenation(PsiElement element) {
        return element.getParent() instanceof PasStringFactor && element.getParent().getParent() instanceof PasLiteralExpr
                && element.getParent().getParent().getParent() instanceof PasBaseReferenceExpr;
    }
}
