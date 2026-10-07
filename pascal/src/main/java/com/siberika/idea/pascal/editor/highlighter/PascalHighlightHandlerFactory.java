package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerFactoryBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class PascalHighlightHandlerFactory extends HighlightUsagesHandlerFactoryBase {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file, @NotNull PsiElement target) {
        if (!(file instanceof PascalFile)) {
            return null;
        }

        if ("EXIT".equalsIgnoreCase(target.getText()) || "RAISE".equalsIgnoreCase(target.getText()) || isResultReference(target)) {
            return new PasHighlightExitPointsHandler(editor, file, target);
        }
        if ("CONTINUE".equalsIgnoreCase(target.getText()) || "BREAK".equalsIgnoreCase(target.getText())) {
            return new PasHighlightBreakOutsHandler(editor, file, target);
        }
        if ("USES".equalsIgnoreCase(target.getText()) || getUnitReference(target) != null) {
            return new PasHighlightUnitIdentsHandler(editor, file, target);
        }
        if ("WITH".equalsIgnoreCase(target.getText()) || getWithStatement(target) != null) {
            return new PasHighlightWithIdentsHandler(editor, file, target);
        }
        return null;
    }

    static boolean isFunction(PasEntityScope scope) {
        return scope instanceof PascalRoutine && ((PascalRoutine) scope).isFunction();
    }

    static boolean isResultReference(PsiElement target) {
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(target);
        if (!isFunction(scope)) {
            return false;
        }
        if (target.getParent() instanceof PasSubIdent) {
            PasSubIdent ident = (PasSubIdent) target.getParent();
            return RoutineUtil.isFunctionResultReference(ident, scope.getName());
        }
        return false;
    }

    static PasNamespaceIdent getUnitReference(PsiElement target) {
        PsiElement parent = target.getParent();
        if (parent instanceof PasSubIdent) {
            parent = parent.getParent();
            return (parent instanceof PasNamespaceIdent) && (parent.getParent() instanceof PasUsesClause) ? (PasNamespaceIdent) parent : null;
        }
        else {
            return null;
        }
    }

    static PasWithStatement getWithStatement(PsiElement target) {
        PsiElement parent = PsiUtil.skipToExpressionParent(target.getParent());
        return (parent instanceof PasWithStatement) ? (PasWithStatement) parent : null;
    }
}
