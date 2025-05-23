package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.*;
import consulo.document.util.TextRange;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 12/25/12
 */
public class PascalPsiImplUtil {

    public static final Logger LOG = Logger.getInstance(PascalPsiImplUtil.class.getName());

    @NotNull
    public static PsiReference[] getReferences(PasModule o) {
        final PascalFile file = PsiTreeUtil.getParentOfType(o, PascalFile.class);
        if (file == null) return PsiReference.EMPTY_ARRAY;
        return new PsiReference[] {
                new PsiReferenceBase<PascalFile>(file, TextRange.from(o.getStartOffsetInParent(), o.getTextLength())) {
                    @Override
                    public PsiElement resolve() {
                        return file.getContainingFile();
                    }

                    @NotNull
                    @Override
                    public Object[] getVariants() {
                        final ArrayList<LookupElement> list = new ArrayList<LookupElement>();

                        return list.toArray(new Object[list.size()]);
                    }
                }
        };
    }

    public static void logNullContainingFile(PascalNamedElement element) {
        LOG.info(String.format("ERROR: Containing file is null for class %s, name %s", element.getClass().getSimpleName(), element.getName()));
    }

    @NotNull
    public static List<PasCustomAttributeDecl> getCustomAttributeDeclList(PasForInlineDeclaration forInlineDeclaration) {
        return Collections.emptyList();
    }

    @NotNull
    public static List<? extends PascalNamedElement> getNamedIdentDeclList(PasForInlineDeclaration forInlineDeclaration) {
        return Collections.singletonList(forInlineDeclaration.getNamedIdent());
    }

    @NotNull
    public static List<? extends PascalNamedElement> getNamedIdentDeclList(PasInlineVarDeclaration inlineVarDeclaration) {
        return inlineVarDeclaration.getNamedIdentList();
    }

}
