package com.siberika.idea.pascal.ide.actions;

import com.siberika.idea.pascal.ide.actions.quickfix.PascalBaseFix;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

public class UsesQuickFixes {
    public static class ExcludeUnitAction extends PascalBaseFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return ObjectPascalLocalize.actionUsesExclude();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            if (null == usedUnitName) {
                return;
            }
            final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
            if (doc != null) {
                doc.insertString(usedUnitName.getTextRange().getStartOffset(), "{!}");
                PsiDocumentManager.getInstance(project).commitDocument(doc);
            }
        }
    }

    public static class OptimizeUsesAction extends PascalBaseFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return ObjectPascalLocalize.actionUsesOptimize();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            if (usedUnitName != null) {
                PascalImportOptimizer.doProcess(usedUnitName.getContainingFile()).run();
            }
        }
    }

    public static class MoveUnitAction extends PascalBaseFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return ObjectPascalLocalize.actionUsesMove();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            TextRange range = getRangeToRemove(usedUnitName);
            if (range != null) {
                final Document doc =
                    PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                if (doc != null) {
                    PascalImportOptimizer.addUnitToSection(PsiUtil.getElementPasModule(usedUnitName),
                        Collections.singletonList(((PascalQualifiedIdent) usedUnitName).getName()), false
                    );
                    doc.deleteString(range.getStartOffset(), range.getEndOffset());
                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                }
            }
        }
    }

    public static class RemoveUnitAction extends PascalBaseFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return ObjectPascalLocalize.actionUsesRemove();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            TextRange range = getRangeToRemove(usedUnitName);
            if (range != null) {
                final Document doc =
                    PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                if (doc != null) {
                    doc.deleteString(range.getStartOffset(), range.getEndOffset());
                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                }
            }
        }
    }

    static TextRange getRangeToRemove(PsiElement usedUnitName) {
        if ((usedUnitName instanceof PascalQualifiedIdent) && (usedUnitName.getParent() instanceof PasUsesClause)) {
            PasUsesClause usesClause = (PasUsesClause) usedUnitName.getParent();
            List<TextRange> ranges = PascalImportOptimizer.getUnitRanges(usesClause);
            TextRange res = PascalImportOptimizer.removeUnitFromSection(
                (PascalQualifiedIdent) usedUnitName,
                usesClause,
                ranges,
                usesClause.getNamespaceIdentList().size()
            );
            if ((res != null) && (usesClause.getNamespaceIdentList()
                .size() == 1)) {                // Remove whole uses clause if last unit removed
                final Document doc =
                    PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                res = doc != null ? TextRange.create(
                    usesClause.getTextRange().getStartOffset(),
                    DocUtil.expandRangeEnd(doc, usesClause.getTextRange().getEndOffset(), DocUtil.RE_LF)
                ) : null;
            }
            return res;
        }
        else {
            return null;
        }
    }

}
