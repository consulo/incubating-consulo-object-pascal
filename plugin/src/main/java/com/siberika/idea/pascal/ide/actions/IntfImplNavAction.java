package com.siberika.idea.pascal.ide.actions;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.ui.ex.action.AnActionEvent;
import consulo.util.collection.SmartList;

import java.util.Collection;
import java.util.Collections;

/**
 * Author: George Bakhtadze
 * Date: 28/05/2015
 */
public class IntfImplNavAction extends PascalAction {

    @Override
    public void doActionPerformed(AnActionEvent e) {
        Editor editor = getEditor(e);
        PsiElement el = getElement(e);
        if ((null == el) || (null == editor)) {
            return;
        }
        PsiElement target;
        target = SectionToggle.getRoutineTarget(PsiTreeUtil.getParentOfType(el, PascalRoutine.class));
        if (null == target) {
            target = SectionToggle.getUsesTarget(PsiTreeUtil.getParentOfType(el, PasUsesClause.class));
        }
        Collection<PsiElement> targets;
        if (PsiUtil.isElementUsable(target)) {
            targets = Collections.singletonList(target);
        } else {
            targets = new SmartList<PsiElement>();
            SectionToggle.getStructTarget(targets, el);
        }
        if (!targets.isEmpty()) {
            EditorUtil.navigateTo(editor, PascalBundle.message("navigate.title.toggle.section"), targets);
        }
    }

}
