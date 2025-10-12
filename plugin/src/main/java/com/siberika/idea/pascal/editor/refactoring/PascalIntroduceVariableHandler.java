package com.siberika.idea.pascal.editor.refactoring;

import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.lang.references.resolve.ResolveProcessor;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StmtUtil;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.refactoring.action.RefactoringActionHandler;
import consulo.language.editor.refactoring.introduce.IntroduceTargetChooser;
import consulo.language.editor.template.TemplateState;
import consulo.language.editor.template.TextResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.object.pascal.psi.PasBaseReferenceExpr;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PascalIntroduceVariableHandler implements RefactoringActionHandler {
    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file, DataContext dataContext) {
        doIntroduceVar(project, editor, file, file.findElementAt(editor.getCaretModel().getOffset()));
    }

    @Override
    public void invoke(@Nonnull Project project, @Nonnull PsiElement[] elements, DataContext dataContext) {
        // not supported
    }

    private void doIntroduceVar(Project project, Editor editor, PsiFile file, PsiElement element) {
        List<PsiElement> expressionList = findExpressions(element);
        PsiElement nearestStatement = StmtUtil.findAssignmentLocation(element);
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(nearestStatement);
        if (!expressionList.isEmpty()) {
            if (editor != null) {
                IntroduceTargetChooser.showChooser(
                    editor,
                    expressionList,
                    expression -> {
                        String type = PascalExpression.inferType(expression);
                        PascalActionDeclare.ActionCreateVar cva =
                            new PascalActionDeclare.ActionCreateVar(LocalizeValue.empty(), expression, null, scope, type) {
                                public void afterExecution(Editor editor1, PsiFile file1, TemplateState state) {
                                    TextResult varName = state.getVariableValue(TPL_VAR_NAME);
                                    if (varName != null) {
                                        replaceExprAndAddAssignment(nearestStatement, varName.getText(), expression);
                                    }
                                }
                            };
                        cva.invoke(project, editor, file);
                    },
                    new PsiElementTrimRenderer(100),
                    ObjectPascalLocalize.popupExpressionsTitle().get()
                );
            }
        }
    }

    private void replaceExprAndAddAssignment(PsiElement nearestStatement, String name, PsiElement expression) {
        final String text = expression.getText();
        PsiElement varElement = PasElementFactory.createElementFromText(expression.getProject(), name);
        PsiElement stmt = PasElementFactory.createElementFromText(expression.getProject(),
            "begin " + name + " := " + text + ";end.", PasCompoundStatement.class
        );
        PsiElement stmtFinal = PsiTreeUtil.getChildOfType(stmt, PasStatement.class);
        if (stmtFinal != null) {
            ApplicationManager.getApplication().runWriteAction(
                () -> {
                    expression.replace(varElement);
                    PsiElement newStmt = nearestStatement.getParent().addBefore(stmtFinal, nearestStatement);
                    IdentQuickFixes.addElements(nearestStatement.getParent(), newStmt, true, ";");
                }
            );
        }
    }

    private List<PsiElement> findExpressions(PsiElement element) {
        List<PsiElement> result = new SmartList<>();
        PsiElement parent = PsiUtil.skipToExpressionParent(element);
        collectExpressions(result, element, parent);
        return result;
    }

    private void collectExpressions(List<PsiElement> result, PsiElement elementUnderCaret, PsiElement parent) {
        if (null == parent) {
            return;
        }
        for (PsiElement child : parent.getChildren()) {
            if (child instanceof PasExpr) {
                addExpression(result, elementUnderCaret, child, true);
            }
            if (child instanceof PasExpression) {
                addExpression(result, elementUnderCaret, ((PasExpression) child).getExpr(), true);
            }
        }
        parent = parent.getParent();
        if (parent instanceof PasExpr) {
            collectExpressions(result, elementUnderCaret, PsiUtil.skipToExpressionParent(parent));
        }
    }

    private void addExpression(List<PsiElement> result, PsiElement elementUnderCaret, PsiElement element, boolean addElement) {
        if (addElement && isAllowed(elementUnderCaret, element)) {
            result.add(element);
        }
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PasExpr) {
                addExpression(result, elementUnderCaret, child, !child.getTextRange().equals(element.getTextRange()));
            }
        }
    }

    private boolean isAllowed(PsiElement elementUnderCaret, PsiElement element) {
        if (!element.getTextRange().contains(elementUnderCaret.getTextRange().getStartOffset())) {
            return false;
        }
        if (element instanceof PasCallExpr) {
            AtomicBoolean result = new AtomicBoolean(false);
            PasExpr callExpr = ((PasCallExpr) element).getExpr();
            if (callExpr instanceof PasBaseReferenceExpr) {        // Filter out procedure calls
                result.set(true);
                Resolve.resolveExpr(NamespaceRec.fromElement(((PasBaseReferenceExpr) callExpr).getFullyQualifiedIdent()),
                    new ResolveContext(PasField.TYPES_ROUTINE, true), new ResolveProcessor() {
                        @Override
                        public boolean process(
                            PasEntityScope originalScope,
                            PasEntityScope scope,
                            PasField field,
                            PasField.FieldType type
                        ) {
                            PascalNamedElement el = field.getElement();
                            if (el instanceof PascalRoutine) {
                                result.set(!"".equals(((PascalRoutine) el).getFunctionTypeStr()));
                                return false;
                            }
                            return true;
                        }
                    }
                );
            }
            return result.get();
        }
        else if (element instanceof PasBaseReferenceExpr) {
            if (element.getParent() instanceof PasCallExpr) {  // Filter out reference expression before ()
                return false;
            }
            else {
                PasFullyQualifiedIdent fqn = ((PasBaseReferenceExpr) element).getFullyQualifiedIdent();
                return fqn.getSubIdentList().size() > 1;      // Filter out non qualified names
            }
        }
        else if ((element instanceof PasExpr) && (element.getParent() instanceof PasParenExpr)) {  // Filter expression inside ()
            return false;
        }
        return true;
    }

}
