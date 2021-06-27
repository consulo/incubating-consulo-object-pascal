package consulo.object.pascal.editor.template;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;

import javax.annotation.Nullable;

public class TextExpression extends Expression {
    private final String myString;

    public TextExpression(String string) {
        myString = string;
    }

    @Override
    public Result calculateResult(ExpressionContext expressionContext) {
        return new TextResult(myString);
    }

    @Nullable
    @Override
    public Result calculateQuickResult(ExpressionContext expressionContext) {
        return calculateResult(expressionContext);
    }

    @Override
    public LookupElement[] calculateLookupItems(ExpressionContext expressionContext) {
        return LookupElement.EMPTY_ARRAY;
    }
}
