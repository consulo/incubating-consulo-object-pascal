package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.template.TemplateState;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

class CreatePropertyForParamAction extends CreateFieldForParamAction {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return ObjectPascalLocalize.actionFixCreateProperty(namedElement != null ? namedElement.getName() : "");
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
        if ((element instanceof PasNamedIdent) && (element.getParent() instanceof PasFormalParameter)) {
            namedElement = (PascalNamedElement) element;
        }
        PascalRoutine routine = getRoutine(namedElement);
        if (routine != null) {
            PasEntityScope classScope = routine.getContainingScope();
            if (classScope instanceof PascalStructType) {
                String fieldName = getFieldName(namedElement.getName());
                PascalActionDeclare.ActionCreatePropertyHP cfa = new PascalActionDeclare.ActionCreatePropertyHP(
                    getText(),
                    namedElement,
                    RoutineUtil.getParameterType(namedElement),
                    classScope
                ) {
                    public void afterExecution(Editor editor, PsiFile file, TemplateState state) {
                        addParamAssignment(classScope, routine, namedElement, fieldName);
                    }
                };
                cfa.invoke(project, editor, routine.getContainingFile());
            }
        }
    }

}
