package com.siberika.idea.pascal.editor.structure;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.language.Language;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.language.psi.PsiFile;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 16/09/2013
 */
@ExtensionImpl
public class PasStructureViewFactory implements PsiStructureViewFactory {
    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        PascalStructureViewBuilder res = new PascalStructureViewBuilder();
        res.setFile(psiFile);
        return res;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
