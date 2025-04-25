package com.siberika.idea.pascal.editor;

import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.Filter;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.LowPriorityAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public class PascalRoutineActions {

    public static class ActionDeclare extends PascalActionDeclare {
        public ActionDeclare(String name, PascalNamedElement element) {
            super(name, element, element.getName(), null, null);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            PasRoutineImplDeclImpl routine = (PasRoutineImplDeclImpl) data.element;

            String prefix = routine.getNamespace() + "(<.*>)?\\.";
            PasProcBodyBlock block = routine.getProcBodyBlock();
            int endoffs = block != null ? block.getStartOffsetInParent() : routine.getTextLength();
            data.text = "\n" + routine.getText().substring(0, endoffs);
            if (prefix.length() > 1) {
                data.text = data.text.replaceFirst(prefix, "");
            }

            data.parent = routine.getContainingScope();
            data.offset = SectionToggle.findIntfPos(routine);
            if (data.offset < 0) {
                data.text = "";
            }
        }
    }

    public static class ActionDeclareAll extends ActionDeclare implements LowPriorityAction {
        public ActionDeclareAll(String name, PascalNamedElement element) {
            super(name, element);
            PascalRoutine routine = (PascalRoutine) element;
            PasEntityScope scope = routine.getContainingScope();
            PasModule module = PsiUtil.getElementPasModule(routine);
            if (null != module) {
                List<PascalRoutine> fields = SectionToggle.collectFields(module.getPrivateFields(), PasField.FieldType.ROUTINE, null);
                for (PascalRoutine field : fields) {
                    if ((field != routine) && (field.getContainingScope() == scope) && (null == SectionToggle.retrieveDeclaration(field, true))) {
                        addData(new FixActionData(field));
                    }
                }
            }
        }
    }

    public static class ActionImplement extends PascalActionDeclare {

        public ActionImplement(String name, PascalNamedElement element) {
            super(name, element, element.getName(), null, null);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            PascalRoutine routine = (PascalRoutine) data.element;
            String prefix = SectionToggle.getPrefix(routine);
            data.text = data.element.getText();
            Collection<PasFunctionDirective> directives = PsiTreeUtil.findChildrenOfType(data.element, PasFunctionDirective.class);
            for (PasFunctionDirective directive : directives) {
                if (directive.getNode().findChildByType(PasTypes.OVERLOAD) == null) {
                    data.text = data.text.replace(directive.getText(), "");
                }
            }
            String name = data.name;
            data.text = "\n\n" + data.text.replaceFirst(" " + name, " " + prefix + name) + "\nbegin\n" + DocUtil.PLACEHOLDER_CARET + "\nend;\n\n";
            data.offset = SectionToggle.findImplPos(routine);
            data.parent = routine;
            if (data.offset < 0) {                                                                            // Suitable implementation position not found - no implementations in the class
                PasEntityScope classType = routine.getContainingScope();
                if (classType instanceof PascalStructType) {
                    data.text = "\n\n{ " + classType.getName() + " }" + data.text;
                }
                data.parent = PsiUtil.getModuleImplementationSection(data.element.getContainingFile());
                data.parent = data.parent != null ? PsiTreeUtil.findChildOfType(data.parent, PasImplDeclSection.class) : null;
                if (null != data.parent) {
                    data.offset = data.parent.getTextRange().getEndOffset();
                } else {                                                // program or library
                    data.offset = SectionToggle.getModuleMainDeclSectionOffset(routine.getContainingFile());
                    if (data.offset >= 0) {
                        data.parent = routine.getContainingFile();
                    }
                }
            }
            if (data.offset < 0) {
                data.text = "";
            }
        }
    }

    public static class ActionImplementAll extends ActionImplement implements LowPriorityAction {
        private boolean initDone = false;

        public ActionImplementAll(String name, PascalNamedElement element) {
            super(name, element);
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            lazyInit();
            return super.isAvailable(project, editor, file);
        }

        synchronized private void lazyInit() {
            if (initDone) {
                return;
            }
            PsiElement element = fixActionDataArray.isEmpty() ? null : fixActionDataArray.get(0).element;
            if (!(PsiUtil.isElementUsable(element) && element instanceof PascalRoutine)) {
                return;
            }
            PascalRoutine routine = (PascalRoutine) element;
            List<PasExportedRoutineImpl> fields = SectionToggle.collectFields(SectionToggle.getDeclFields(routine.getContainingScope()),
                    PasField.FieldType.ROUTINE, new Filter<PasField>() {
                        @Override
                        public boolean allow(PasField value) {
                            return value.getElement() instanceof PasExportedRoutineImpl;
                        }
                    });
            for (PasExportedRoutineImpl field : fields) {
                if ((field != routine) && (PsiUtil.needImplementation(field)) && (null == SectionToggle.retrieveImplementation(field, true))) {
                    addData(new FixActionData(field));
                }
            }
            initDone = true;
        }

    }

}
