package com.siberika.idea.pascal.ide.actions;

import consulo.codeEditor.Editor;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.logging.Logger;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public abstract class PascalAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(PascalAction.class);

    @Nullable
    protected static PsiElement getElement(AnActionEvent e) {
        PsiFile file = getFile(e);
        Editor editor = getEditor(e);
        if ((null == file) || (null == editor)) {
            return null;
        }
        return file.findElementAt(editor.getCaretModel().getOffset());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            doActionPerformed(e);
        } catch (Exception e1) {
            LOG.info("Action error", e1);
        }
    }

    protected abstract void doActionPerformed(AnActionEvent e);

    protected static Editor getEditor(AnActionEvent e) {
        return e.getData(PlatformDataKeys.EDITOR);
    }

    protected static PsiFile getFile(AnActionEvent e) {
        return e.getData(LangDataKeys.PSI_FILE);
    }
}
