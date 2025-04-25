package com.siberika.idea.pascal.lang.inspection;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.*;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public abstract class PascalLocalInspectionBase extends LocalInspectionTool {
    @Nullable
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Nonnull
    @Override
    public String getGroupDisplayName() {
        return "";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    protected void checkUses(PasUsesClause usesClause, ProblemsHolder holder, boolean isOnTheFly) {
    }

    protected void checkRoutine(PascalRoutine routine, ProblemsHolder holder, boolean isOnTheFly) {
    }

    protected void checkNamedIdent(PascalNamedElement namedIdent, ProblemsHolder holder, boolean isOnTheFly) {
    }

    protected void checkClass(PasClassTypeDecl classTypeDecl, ProblemsHolder holder, boolean isOnTheFly) {
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session, Object state) {
        return new PasVisitor() {

            @Override
            public void visitUsesClause(@NotNull PasUsesClause usesClause) {
                checkUses(usesClause, holder, isOnTheFly);
            }

            @Override
            public void visitNamedIdent(@NotNull PasNamedIdent namedIdent) {
                checkNamedIdent(namedIdent, holder, isOnTheFly);
            }

            @Override
            public void visitNamedIdentDecl(@NotNull PasNamedIdentDecl namedIdent) {
                checkNamedIdent(namedIdent, holder, isOnTheFly);
            }

            @Override
            public void visitcalRoutine(@NotNull PascalRoutine routine) {
                checkRoutine(routine, holder, isOnTheFly);
            }

            @Override
            public void visitClassTypeDecl(@NotNull PasClassTypeDecl classTypeDecl) {
                checkClass(classTypeDecl, holder, isOnTheFly);
            }
        };
    }
}
