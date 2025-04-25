package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.search.DescendingEntities;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

class GotoImplementationAction extends NavIntentionActionBase {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.struct.goto.descending");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return getScopeElement(element) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PasEntityScope scope = getScopeElement(element);
        Collection<PsiElement> targets = DescendingEntities.getQuery(scope, GlobalSearchScope.allScope(PsiUtilCore.getProjectInReadAction(element))).findAll();
        EditorUtil.navigateTo(editor, getText(), targets);
    }

    private static PasEntityScope getScopeElement(PsiElement element) {
        element = PsiTreeUtil.skipParentsOfType(element, PsiWhiteSpace.class, PsiErrorElement.class);
        if ((element instanceof PascalRoutine) || (element instanceof PascalStructType)) {
            return (PasEntityScope) element;
        }
        return ((element instanceof PascalNamedElement) && (element.getParent() instanceof PasGenericTypeIdent)) ? PsiUtil.getStructByElement(element) : null;
    }
}
