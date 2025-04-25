package com.siberika.idea.pascal.lang.references.impl.manipulators;

import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 23/08/2013
 */
@ExtensionImpl
public class PascalNamedManipulator extends AbstractElementManipulator<PascalNamedElement> {
    @Override
    public PascalNamedElement handleContentChange(@NotNull PascalNamedElement element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        if ((element.getContainingFile() != null) && (element.getContainingFile().getVirtualFile() != null)) {
            @SuppressWarnings("ConstantConditions")
            final Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
            if (document != null) {
                document.replaceString(element.getTextRange().getStartOffset() + range.getStartOffset(), element.getTextRange().getStartOffset() + range.getEndOffset(), newContent);
                PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
            }
        }
        return element;
    }

    @Nonnull
    @Override
    public Class<PascalNamedElement> getElementClass() {
        return PascalNamedElement.class;
    }


}
