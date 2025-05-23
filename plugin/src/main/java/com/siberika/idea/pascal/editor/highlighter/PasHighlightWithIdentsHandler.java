package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.resolve.PsiElementProcessor;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Highlight all identifiers in WITH statement which come from the scope under cursor or all identifiers from WITH scopes when cursor is on WITH keyword
 */
public class PasHighlightWithIdentsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private final PsiElement target;

    PasHighlightWithIdentsHandler(Editor editor, PsiFile file, PsiElement target) {
        super(editor, file);
        this.target = target;
    }

    @Override
    public List<PsiElement> getTargets() {
        return Collections.singletonList(target);
    }

    @Override
    protected void selectTargets(List<PsiElement> targets, Consumer<List<PsiElement>> selectionConsumer) {
        selectionConsumer.accept(targets);
    }

    @Override
    public void computeUsages(List<PsiElement> targets) {
        PasWithStatement with;
        PsiElement expr = null;
        if (target.getParent() instanceof PasWithStatement) {
            with = (PasWithStatement) target.getParent();
        } else {
            with = PascalHighlightHandlerFactory.getWithStatement(target);
            if (null == with) {
                return;
            }
            expr = PsiUtil.skipToExpression(target.getParent());
            expr = expr != null ? expr.getParent() : null;
            if (expr != null && expr.getParent() != with) {
                expr = null;
            }
            addOccurrence(target);
        }

        addOccurrence(with.getFirstChild());

        addOccurrence(target);
        Collection<PasFullyQualifiedIdent> idents = PsiTreeUtil.findChildrenOfAnyType(with, PasFullyQualifiedIdent.class);
        for (PasFullyQualifiedIdent ident : idents) {
            if (expr instanceof PasExpression) {
                processElementsFromWith((PasExpression) expr, ident, element -> {
                    addOccurrence(element);
                    return true;
                });
            } else {
                for (PasExpression withExpr : with.getExpressionList()) {
                    processElementsFromWith(withExpr, ident, element -> {
                        addOccurrence(element);
                        return true;
                    });
                }
            }
        }
    }

    public static void processElementsFromWith(PasExpression withExpr, PasFullyQualifiedIdent namedElement, PsiElementProcessor<PasSubIdent> processor) {
        PasExpr expression = withExpr != null ? withExpr.getExpr() : null;
        if (expression instanceof PascalExpression) {
            List<PasField.ValueType> types = PascalExpression.getTypes((PascalExpression) withExpr.getExpr());
            if (!types.isEmpty()) {
                PasEntityScope ns = PascalExpression.retrieveScope(types);
                if (ns instanceof PascalStructType) {
                    processWithNamespace(ns, namedElement, processor);
                    for (SmartPsiElementPointer<PasEntityScope> scopePtr : ns.getParentScope()) {
                        processWithNamespace(scopePtr.getElement(), namedElement, processor);
                    }
                }
            }
        }
    }

    private static void processWithNamespace(PasEntityScope scope, PasFullyQualifiedIdent namedElement, PsiElementProcessor<PasSubIdent> processor) {
        List<PasSubIdent> subidents = namedElement.getSubIdentList();
        if (!subidents.isEmpty()) {
            PasSubIdent sub = subidents.get(0);
            if (scope instanceof PascalStructType) {
                PasField field = scope.getField(sub.getName());
                if (field != null) {
                    processor.execute(sub);
                }
            }
        }
    }

}
