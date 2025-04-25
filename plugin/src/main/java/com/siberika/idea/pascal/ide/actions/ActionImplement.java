package com.siberika.idea.pascal.ide.actions;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.ui.TreeViewStruct;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.Filter;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.document.Document;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.WriteCommandAction;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.ui.ex.action.AnActionEvent;
import consulo.undoRedo.CommandProcessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.siberika.idea.pascal.PascalBundle.message;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public class ActionImplement extends PascalAction {
    @Override
    public void doActionPerformed(AnActionEvent e) {
        PsiElement el = getElement(e);
        Editor editor = getEditor(e);
        showOverrideDialog(el, editor);
    }

    public void showOverrideDialog(PsiElement el, Editor editor) {
        PascalRoutine methodImpl = null;
        PasEntityScope scope = PsiTreeUtil.getParentOfType(el, PasEntityScope.class);
        if (scope instanceof PascalRoutine) {
            methodImpl = (PascalRoutine) scope;
            scope = scope.getContainingScope();
        }
        if (!(scope instanceof PascalStructType)) {
            EditorUtil.showErrorHint(PascalBundle.message("action.error.notinstruct"), EditorUtil.getHintPos(editor));
            return;
        }
        Collection<PasEntityScope> structs = new LinkedHashSet<>(Arrays.asList(GotoSuper.searchForStruct((PascalStructType) scope).toArray(new PasEntityScope[0])));
        final Set<String> existing = new HashSet<String>();
        for (PasField field : scope.getAllFields()) {
            allowNonExistingRoutines(field, existing);
        }

        TreeViewStruct tree = new TreeViewStruct(el.getProject(), PascalBundle.message("title.override.methods", scope.getName()), structs, new Filter<PasField>() {
            @Override
            public boolean allow(PasField value) {
                return allowNonExistingRoutines(value, existing);
            }
        });
        tree.show();

        doOverride(editor, scope, el, methodImpl, tree.getSelected());
    }

    private boolean allowNonExistingRoutines(PasField value, Set<String> existing) {
        if (value.fieldType == PasField.FieldType.ROUTINE) {
            String name = PsiUtil.getFieldName(value.getElement());
            if (!existing.contains(name)) {
                existing.add(name);
                return true;
            }
        }
        return false;
    }
    // if methodImpl = null assuming interface part

    private void doOverride(final Editor editor, final PasEntityScope scope, final PsiElement el, PascalRoutine methodImpl, final List<PasField> selected) {
        PsiElement prevMethod = getPrevMethod(el, methodImpl);
        final AtomicInteger offs = new AtomicInteger(RoutineUtil.calcMethodPos(scope, prevMethod));
        if (offs.get() < 0) {
            EditorUtil.showErrorHint(PascalBundle.message("action.error.find.position"), EditorUtil.getHintPos(editor));
            return;
        }

        PsiFile file = el.getContainingFile();
        final Document document = editor.getDocument();
        for (final PasField field : selected) {
            WriteCommandAction.runWriteCommandAction(el.getProject(), new Runnable() {
                        @Override
                        public void run() {
                            CommandProcessor.getInstance().setCurrentCommandName(PascalBundle.message("action.override"));
                            PascalNamedElement element = field.getElement();
                            if (PsiUtil.isElementUsable(element)) {
                                CharSequence text = RoutineUtil.prepareRoutineHeaderText(element.getText(), "override", "");
                                document.insertString(offs.get(), text);
                                offs.addAndGet(text.length());
                            }
                            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                            PsiDocumentManager.getInstance(el.getProject()).commitDocument(document);
                        }
                    }
            );
        }
        DocUtil.reformat(scope, true);
        for (final PasField field : selected) {
            PascalNamedElement element = field.getElement();
            if (PsiUtil.isElementUsable(element)) {
                PasField routine = scope.getField(PsiUtil.getFieldName(field.getElement()));
                PascalRoutineActions.ActionImplement act = routine != null ? new PascalRoutineActions.ActionImplement(message("action.implement"), routine.getElement()) : null;
                if (act != null) {
                    act.invoke(el.getProject(), editor, file);
                }
            }
        }
    }

    private PsiElement getPrevMethod(PsiElement el, PascalRoutine methodImpl) {
        if (methodImpl != null) {
            return SectionToggle.retrieveDeclaration(methodImpl, false);
        }
        PasExportedRoutine routine = (el instanceof PasExportedRoutine) ? (PasExportedRoutine) el : PsiTreeUtil.getParentOfType(el, PasExportedRoutine.class);
        if (null == routine) {
            routine = PsiTreeUtil.getPrevSiblingOfType(el, PasExportedRoutine.class);
        }
        return routine;
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(PascalLanguage.INSTANCE.equals(e.getData(LangDataKeys.LANGUAGE)));
    }

}
