package com.siberika.idea.pascal.lang.inspection;

import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.ide.actions.quickfix.PascalBaseFix;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.search.PascalDefinitionsSearch;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.application.util.function.Processor;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.document.Document;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import consulo.undoRedo.CommandProcessor;
import consulo.util.collection.SmartHashSet;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nonnull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("Convert2Lambda")
public class NotImplementedInspection extends PascalLocalInspectionBase {

    @Override
    protected void checkClass(PasClassTypeDecl classTypeDecl, ProblemsHolder holder, boolean isOnTheFly) {
        PasClassParent classParent = classTypeDecl.getClassParent();
        if ((null != classParent) && hasNoDescendants(classTypeDecl)) {
            SmartList<PasExportedRoutine> notImplemented = new SmartList<>();
            processClass(notImplemented, new SmartHashSet<>(), classTypeDecl);
            Set<String> processed = new SmartHashSet<>();
            for (PasExportedRoutine routine : notImplemented) {
                String name = routine.getCanonicalName();
                if (!processed.contains(name)) {
                    processed.add(name);
                    PasEntityScope scope = routine.getContainingScope();
                    String scopeName = scope != null ? ResolveUtil.cleanupName(scope.getName()) + "." : "";
                    name = scopeName + name;
                    holder.registerProblem(holder.getManager().createProblemDescriptor(
                        classParent,
                        ObjectPascalLocalize.inspectionWarnMethodsNotImplemented(name).get(),
                        true,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOnTheFly,
                        new ImplementMethodFix(routine)
                    ));
                }
            }
        }
    }

    private void processClass(
        List<PasExportedRoutine> result,
        SmartHashSet<PascalStructType> processed,
        @Nonnull PasClassTypeDecl classTypeDecl
    ) {
        processed.add(classTypeDecl);
        List<SmartPsiElementPointer<PasEntityScope>> parentScope = classTypeDecl.getParentScope();
        for (SmartPsiElementPointer<PasEntityScope> parentPtr : parentScope) {
            PasEntityScope el = parentPtr.getElement();
            if (el instanceof PascalInterfaceDecl) {
                processInterface(result, processed, classTypeDecl, (PascalInterfaceDecl) el);
            }
        }

        Iterator<SmartPsiElementPointer<PasEntityScope>> parentIter = parentScope.iterator();
        if (parentIter.hasNext()) {
            PasEntityScope el = parentIter.next().getElement();
            if (el instanceof PasClassTypeDecl && !processed.contains(el)) {
                processClass(result, processed, (PasClassTypeDecl) el);
            }
        }
        for (Iterator<PasExportedRoutine> iterator = result.iterator(); iterator.hasNext(); ) {
            PasExportedRoutine method = iterator.next();
            if (isMethodImplemented(classTypeDecl, method)) {
                iterator.remove();
            }
        }
    }

    private void processInterface(
        List<PasExportedRoutine> result,
        SmartHashSet<PascalStructType> processed,
        @Nonnull PasClassTypeDecl classDecl,
        PascalInterfaceDecl interfaceDecl
    ) {
        for (PasExportedRoutine method : interfaceDecl.getMethods()) {
            if (isMethodImplemented(classDecl, method)) {
            }
            else {
                result.add(method);
            }
        }
        processed.add(interfaceDecl);
        for (SmartPsiElementPointer<PasEntityScope> parent : interfaceDecl.getParentScope()) {
            PasEntityScope el = parent.getElement();
            if (el instanceof PascalInterfaceDecl && !processed.contains(el)) {
                processInterface(result, processed, classDecl, (PascalInterfaceDecl) el);
            }
        }
    }

    private boolean isMethodImplemented(PasClassTypeDecl classTypeDecl, PasExportedRoutine method) {
        return classTypeDecl.getRoutine(method.getReducedName()) != null;
    }

    private boolean hasNoDescendants(PasClassTypeDecl classTypeDecl) {
        return PascalDefinitionsSearch.processDescendingStructs(classTypeDecl, false, new Processor<>() {
            @Override
            public boolean process(PasEntityScope pascalStructType) {
                return false;
            }
        });
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Not implemented methods detection");
    }

    private class ImplementMethodFix extends PascalBaseFix {
        private final SmartPsiElementPointer<PasExportedRoutine> routinePtr;

        ImplementMethodFix(PasExportedRoutine routine) {
            this.routinePtr = PsiUtil.createSmartPointer(routine);
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            PasExportedRoutine routine = routinePtr != null ? routinePtr.getElement() : null;
            return ObjectPascalLocalize.actionImplementMethod(routine != null ? routine.getCanonicalName() : "?");
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PasExportedRoutine routine = routinePtr != null ? routinePtr.getElement() : null;
            PasEntityScope scope = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PasEntityScope.class);
            if ((null == routine) || (null == scope)) {
                return;
            }
            final int offs = RoutineUtil.calcMethodPos(scope, null);
            Editor editor = EditorUtil.getEditor(project);
            if (offs < 0) {
                EditorUtil.showErrorHint(ObjectPascalLocalize.actionErrorFindPosition(), EditorUtil.getHintPos(editor));
                return;
            }
            final Document document = DocUtil.getDocument(scope);
            if (document != null) {
                CommandProcessor.getInstance().setCurrentCommandName(getName());
                CharSequence text = RoutineUtil.prepareRoutineHeaderText(routine.getText(), "", "override") + "\n";
                document.insertString(offs, text);

                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                PsiDocumentManager.getInstance(project).commitDocument(document);
                DocUtil.reformat(scope.getParent(), true);
                PasField addedRoutineField = scope.getField(PsiUtil.getFieldName(routine));
                PascalNamedElement addedRoutine = addedRoutineField != null ? addedRoutineField.getElement() : null;
                if (PsiUtil.isElementUsable(addedRoutine)) {
                    PsiFile file = scope.getContainingFile();
                    PascalRoutineActions.ActionImplement act =
                        new PascalRoutineActions.ActionImplement(ObjectPascalLocalize.actionImplement(), addedRoutine);
                    act.invoke(project, editor, file);
                }
            }
        }
    }
}
