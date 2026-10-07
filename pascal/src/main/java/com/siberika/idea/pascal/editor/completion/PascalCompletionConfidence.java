package com.siberika.idea.pascal.editor.completion;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.completion.CompletionConfidence;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.util.lang.ThreeState;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
@ExtensionImpl
public class PascalCompletionConfidence extends CompletionConfidence {
    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (PascalLexer.NUMBERS.contains(contextElement.getNode().getElementType())) {
            return ThreeState.YES;
        }
        if ((contextElement.getPrevSibling() != null) && (contextElement.getPrevSibling().getNode() != null)) {

            /*System.out.println("===*** shouldSkipAutopopup: " + contextElement + ", sib: " + contextElement.getPrevSibling()
                    + ", par: " + contextElement.getParent());*/
            IElementType type = contextElement.getPrevSibling().getNode().getElementType();
            if (!isName(type)) {
                type = contextElement.getNode().getElementType();
            }
            if (!isName(type) && !PascalLexer.COMPILER_DIRECTIVES.contains(type) && shouldSkipInComment(contextElement, offset)) {
                return ThreeState.YES;
            }
        }
        return super.shouldSkipAutopopup(contextElement, psiFile, offset);
    }

    private static final Pattern COMMENT_BEGIN = Pattern.compile("\\{\\$?\\w+");

    private boolean shouldSkipInComment(PsiElement contextElement, int offset) {
        int len = offset - contextElement.getTextRange().getStartOffset();
        String text = contextElement.getText().substring(0, len);
        return !COMMENT_BEGIN.matcher(text).matches();
    }

    private boolean isName(IElementType type) {
        return (type == PasTypes.SUB_IDENT) || (type == PasTypes.NAME)
                || (type == PasTypes.CALL_EXPR) || (type == PasTypes.INDEX_EXPR) || (type == PasTypes.DEREFERENCE_EXPR)
                || (type == PasTypes.PAREN_EXPR) || (type == PasTypes.EXPRESSION);
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
