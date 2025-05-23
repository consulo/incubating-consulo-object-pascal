package com.siberika.idea.pascal.lang.inspection;

import com.siberika.idea.pascal.ide.actions.UsesQuickFixes;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.util.ModuleUtilCore;
import jakarta.annotation.Nonnull;

import static com.siberika.idea.pascal.PascalBundle.message;

@ExtensionImpl
public class UnusedUnitsInspection extends PascalLocalInspectionBase {

    @Nonnull
    @Override
    public String getDisplayName() {
        return "Unused units detection";
    }

    @Override
    public void checkUses(PasUsesClause usesClause, ProblemsHolder holder, boolean isOnTheFly) {
        if (!PsiUtil.isElementValid(usesClause)) {
            return;
        }
        for (PasNamespaceIdent usedUnit : usesClause.getNamespaceIdentList()) {
            ProblemDescriptor res = annotateUnit(holder.getManager(), usedUnit, isOnTheFly);
            if (res != null) {
                holder.registerProblem(res);
            }
        }

    }

    private ProblemDescriptor annotateUnit(InspectionManager holder, PascalQualifiedIdent usedUnitName, boolean isOnTheFly) {
        switch (PascalImportOptimizer.getUsedUnitStatus(usedUnitName, ModuleUtilCore.findModuleForPsiElement(usedUnitName))) {
            case UNUSED: {
                return holder.createProblemDescriptor(usedUnitName, message("inspection.warn.unused.unit"), true,
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
                        new UsesQuickFixes.RemoveUnitAction(), new UsesQuickFixes.ExcludeUnitAction(), new UsesQuickFixes.OptimizeUsesAction());
            }
            case USED_IN_IMPL: {
                return holder.createProblemDescriptor(usedUnitName, message("inspection.warn.unused.unit.interface"),
                        true, ProblemHighlightType.WEAK_WARNING, isOnTheFly,
                        new UsesQuickFixes.MoveUnitAction(), new UsesQuickFixes.RemoveUnitAction(), new UsesQuickFixes.ExcludeUnitAction(), new UsesQuickFixes.OptimizeUsesAction());
            }
        }
        return null;
    }

}
