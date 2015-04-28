package com.siberika.idea.pascal.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasCaseStatement;
import com.siberika.idea.pascal.lang.psi.PasClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasClassTypeTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasHandler;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasObjectDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitFinalization;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInitialization;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 24/03/2013
 */
public class PascalFoldingBuilder extends FoldingBuilderEx {

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        final List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();

        foldCommon(root, descriptors);
        foldCase(root, descriptors);
        foldEnums(root, descriptors);

        if (!quick) {
            foldComments(root, descriptors);
        }

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private void foldCommon(PsiElement root, List<FoldingDescriptor> descriptors) {
        @SuppressWarnings("unchecked")
        Collection<PascalPsiElement> blocks = PsiUtil.findChildrenOfAnyType(root,
                PasUnitInterface.class, PasUnitImplementation.class, PasUsesClause.class, PasUnitInitialization.class, PasUnitFinalization.class,
                PasVarSection.class, PasTypeSection.class, PasConstSection.class,
                PasClassTypeTypeDecl.class, PasClassHelperDecl.class, PasClassTypeDecl.class,
                PasInterfaceTypeDecl.class, PasObjectDecl.class, PasRecordHelperDecl.class, PasRecordDecl.class,
                PasCompoundStatement.class, PasHandler.class, PasRepeatStatement.class);

        for (final PsiElement block : blocks) {
            int foldStart = block.getFirstChild() != null ? block.getFirstChild().getTextRange().getEndOffset() : block.getTextRange().getStartOffset();
            TextRange range = getRange(foldStart, block.getTextRange().getEndOffset());
            if (range.getLength() > 1) {
                descriptors.add(new FoldingDescriptor(block.getNode(), range, null));
            }
        }
    }

    private TextRange getRange(int start, int end) {
        return new TextRange(start, end);
    }

    private void foldCase(PsiElement root, List<FoldingDescriptor> descriptors) {
        Collection<PasCaseStatement> caseStatements = PsiTreeUtil.findChildrenOfType(root, PasCaseStatement.class);
        for (final PasCaseStatement caseStatement : caseStatements) {
            PsiElement caseItem = PsiUtil.getNextSibling(caseStatement.getFirstChild());
            if (caseItem != null) {
                caseItem = PsiUtil.getNextSibling(caseItem);
            }
            int foldStart = caseItem != null ? caseItem.getTextRange().getStartOffset() : caseStatement.getTextRange().getStartOffset();
            TextRange range = getRange(foldStart, caseStatement.getTextRange().getEndOffset());
            if (range.getLength() > 0) {
                descriptors.add(new FoldingDescriptor(caseStatement.getNode(), range, null));
            }
        }
    }

    private void foldEnums(PsiElement root, List<FoldingDescriptor> descriptors) {
        @SuppressWarnings("unchecked")
        Collection<PasEnumType> enums = PsiUtil.findChildrenOfAnyType(root, PasEnumType.class);
        for (final PascalPsiElement enumType : enums) {
            final PasTypeDeclaration decl = PsiTreeUtil.getParentOfType(enumType, PasTypeDeclaration.class);
            if (decl != null) {
                TextRange range = getRange(decl.getGenericTypeIdent().getTextRange().getEndOffset(), decl.getTextRange().getEndOffset());
                if (range.getLength() > 0) {
                    descriptors.add(new FoldingDescriptor(decl.getNode(), range, null) {
                        @Nullable
                        @Override
                        public String getPlaceholderText() {
                            return "=(...);";
                        }
                    });
                }
            }
        }
    }

    private void foldComments(PsiElement root, List<FoldingDescriptor> descriptors) {
        final Collection<PsiComment> comments = PsiTreeUtil.findChildrenOfType(root, PsiComment.class);
        TextRange commentRange = null;
        PsiComment lastComment = null;
        for (final PsiComment comment : comments) {
            if ((null == lastComment) || (commentRange.getEndOffset() < comment.getTextRange().getStartOffset())) {
                lastComment = comment;
                final String endSymbol = getEndSymbol(lastComment);
                commentRange = comment.getTextRange();
                // Merge sibling comments
                PsiElement sibling = PsiUtil.getNextSibling(comment);
                while (sibling instanceof PsiComment) {
                    commentRange = commentRange.union(sibling.getTextRange());
                    sibling = PsiUtil.getNextSibling(sibling);
                }

                int lfPos = lastComment.getText().indexOf('\n') + lastComment.getTextOffset();
                if (lfPos < lastComment.getTextOffset()) {
                    lfPos = lastComment.getTextRange().getEndOffset();
                }
                if (lfPos < commentRange.getEndOffset()) {
                    descriptors.add(new FoldingDescriptor(lastComment.getNode(), getRange(lfPos, commentRange.getEndOffset()), null) {
                        @Nullable
                        @Override
                        public String getPlaceholderText() {
                            return "..." + endSymbol;
                        }
                    });
                }
            }
        }
    }

    private String getEndSymbol(PsiComment comment) {
        if (StringUtils.isNotEmpty(comment.getText())) {
            if (comment.getText().startsWith("{")) {
                return "}";
            } else if (comment.getText().startsWith("(*")) {
                return "*)";
            }
        }
        return "";
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return node.getElementType() == PasTypes.COMMENT;
    }
}