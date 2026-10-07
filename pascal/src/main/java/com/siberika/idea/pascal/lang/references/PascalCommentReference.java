package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.lang.lexer.PascalFlexLexerImpl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.sdk.Define;
import com.siberika.idea.pascal.util.ModuleUtil;
import consulo.document.util.TextRange;
import consulo.language.psi.*;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
class PascalCommentReference extends PsiReferenceBase<PsiComment> {

    private final String name;

    PascalCommentReference(@NotNull PsiElement element) {
        super((PsiComment) element);
        TextRange range = getRangeInElement();
        name = element.getText().substring(range.getStartOffset(), range.getEndOffset());
    }

    PascalCommentReference(PsiComment element, TextRange rangeInElement) {
        super(element, rangeInElement);
        TextRange range = getRangeInElement();
        name = element.getText().substring(range.getStartOffset(), range.getEndOffset());
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return doResolve(name);
    }

    private PsiElement doResolve(String name) {
        if (null == name) {
            return null;
        }
        if (getElement().getNode().getElementType() == PasTypes.CT_DEFINE) {
            PascalFlexLexerImpl lexer = PascalFlexLexerImpl.processFile(myElement.getProject(), myElement.getContainingFile().getVirtualFile());
            if (lexer != null) {
                for (Map.Entry<String, Define> entry : lexer.getAllDefines().entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(name)) {
                        if (entry.getValue().virtualFile != null) {
                            PsiFile file = PsiManager.getInstance(myElement.getProject()).findFile(entry.getValue().virtualFile);
                            if (file != null) {
                                return PsiUtilCore.getElementAtOffset(file, entry.getValue().offset);
                            }
                        }
                    }
                }
            }
            return getElement();
        } else {
            VirtualFile file = ModuleUtil.getIncludedFile(myElement.getProject(), myElement.getContainingFile().getVirtualFile(), name);
            return file != null ? PsiManager.getInstance(myElement.getProject()).findFile(file) : null;
        }
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PascalCommentReference that = (PascalCommentReference) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
