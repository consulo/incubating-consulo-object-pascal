package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.util.lang.Pair;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Highlight all identifiers which come from the unit under cursor or all external identifiers when cursor is on USES keyword
 */
public class PasHighlightUnitIdentsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private final PsiElement target;

    PasHighlightUnitIdentsHandler(Editor editor, PsiFile file, PsiElement target) {
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
        PasModule pasModule = PsiUtil.getElementPasModule(target);
        if (null == pasModule) {
            return;
        }

        PasNamespaceIdent unitName = null;
        if (!(target.getParent() instanceof PasUsesClause)) {
            unitName = PascalHighlightHandlerFactory.getUnitReference(target);
            if (null == unitName) {
                return;
            }
        }

        Module module = ModuleUtilCore.findModuleForPsiElement(target);
        addOccurrence(target);
        Pair<List<PascalNamedElement>, List<PascalNamedElement>> idents = pasModule.getIdentsFrom(unitName != null ? unitName.getName() : null,
                ContextUtil.belongsToInterface(target), ModuleUtil.retrieveUnitNamespaces(module, target.getProject()));
        for (PascalNamedElement ident : idents.first) {
            addOccurrence(ident);
        }
        for (PascalNamedElement ident : idents.second) {
            addOccurrence(ident);
        }
    }

}
