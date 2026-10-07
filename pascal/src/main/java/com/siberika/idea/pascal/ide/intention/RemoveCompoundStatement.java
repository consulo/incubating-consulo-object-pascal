package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasStmtEmpty;
import com.siberika.idea.pascal.util.StmtUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseElementAtCaretIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

import java.util.List;

class RemoveCompoundStatement extends BaseElementAtCaretIntentionAction {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return ObjectPascalLocalize.actionFixStatementRemoveCompound();
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
        PsiElement parent = StmtUtil.getStatement(element);
        if (StmtUtil.isStructuredOperatorStatement(parent)) {
            PasStatement stmt = PsiTreeUtil.getChildOfType(parent, PasStatement.class);
            if (stmt != null) {
                PasCompoundStatement compoundStatement = stmt.getCompoundStatement();
                return (compoundStatement != null) && isSingleOrEmptyCompoundStatement(compoundStatement);
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
        PsiElement parent = StmtUtil.getStatement(element);
        PasStatement stmt = StmtUtil.isStructuredOperatorStatement(parent) ? PsiTreeUtil.getChildOfType(parent, PasStatement.class) : null;
        PasCompoundStatement compoundStatement = stmt != null ? stmt.getCompoundStatement() : null;
        PasStatement statement;
        if ((compoundStatement != null) && isSingleOrEmptyCompoundStatement(compoundStatement)) {
            statement = !compoundStatement.getStatementList().isEmpty() ? compoundStatement.getStatementList().get(0) : null;
            if (statement != null) {
                compoundStatement.replace(statement);
            }
            else {
                compoundStatement.delete();
            }
        }
    }

    private static boolean isSingleOrEmptyCompoundStatement(PasCompoundStatement statement) {
        List<PasStatement> list = statement.getStatementList();
        return (list.size() < 2) || ((list.size() == 2) && (list.get(1).getFirstChild() instanceof PasStmtEmpty));
    }
}
