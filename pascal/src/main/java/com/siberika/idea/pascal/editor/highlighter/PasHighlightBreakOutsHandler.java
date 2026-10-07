package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.psi.*;
import consulo.codeEditor.Editor;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.editor.util.ProductivityFeatureNames;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Highlight all BREAK and CONTINUE keywords within a loop as well as the loop keyword
 */
public class PasHighlightBreakOutsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private final PsiElement target;

    PasHighlightBreakOutsHandler(Editor editor, PsiFile file, PsiElement target) {
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
        PsiElement parent = target.getParent();
        if (!(parent instanceof PasBreakStatement) && !(parent instanceof PasContinueStatement)) {
            return;
        }

        PsiElement loop = getLoop(target);
        if (null == loop) {
            return;
        }
        addOccurrence(loop.getFirstChild());
        Collection<PascalPsiElement> sts = PsiTreeUtil.findChildrenOfAnyType(loop, PasExitStatement.class, PasRaiseStatement.class, PasBreakStatement.class, PasContinueStatement.class);
        for (PascalPsiElement st : sts) {
            if (getLoop(st) == loop) {
                addOccurrence(st.getFirstChild());
            }
        }
    }

    private PsiElement getLoop(PsiElement target) {
        PsiElement result = PsiTreeUtil.getNonStrictParentOfType(target, PasEntityScope.class, PasForStatement.class, PasWhileStatement.class, PasRepeatStatement.class);
        if (result instanceof PasForStatement || result instanceof PasWhileStatement || result instanceof PasRepeatStatement) {
            return result;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getFeatureId() {
        return ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN;
    }

}
