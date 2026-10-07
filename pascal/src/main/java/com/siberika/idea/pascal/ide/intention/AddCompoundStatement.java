package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import com.siberika.idea.pascal.util.PsiUtil;
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

class AddCompoundStatement extends BaseElementAtCaretIntentionAction {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return ObjectPascalLocalize.actionFixStatementAddCompound();
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
        PsiElement parent = StmtUtil.getStatement(element);
        if (StmtUtil.isStructuredOperatorStatement(parent)) {
            PasStatement stmt = PsiTreeUtil.getChildOfType(parent, PasStatement.class);
            return (stmt != null) && (stmt.getCompoundStatement() == null);
        } else {
            return false;
        }
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
        PsiElement parent = StmtUtil.getStatement(element);
        PasStatement stmt = StmtUtil.isStructuredOperatorStatement(parent) ? PsiTreeUtil.getChildOfType(parent, PasStatement.class) : null;
        if (PsiUtil.isElementUsable(stmt)) {
            PasCompoundStatement compoundStatement = PasElementFactory.createElementFromText(project, "begin end", PasCompoundStatement.class);
            if (compoundStatement != null) {
                List<PasStatement> stmts = compoundStatement.getStatementList();
                if (!stmts.isEmpty()) {
                    PsiElement oldStmt = stmt.copy();
                    stmts.get(0).replace(oldStmt);
                    stmt.replace(compoundStatement);
                }
            }
        }
    }
}
