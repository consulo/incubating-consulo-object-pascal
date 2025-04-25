package com.siberika.idea.pascal.editor.structure;

import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.editor.structureView.StructureViewModelBase;
import consulo.language.psi.PsiFile;

/**
 * Author: George Bakhtadze
 * Date: 07/05/2015
 */
public class PascalStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    public PascalStructureViewModel(PsiFile psiFile, StructureViewTreeElement root) {
        super(psiFile, root);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element instanceof PasStructureViewTreeElement;
    }
}
