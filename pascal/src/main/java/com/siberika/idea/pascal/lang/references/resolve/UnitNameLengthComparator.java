package com.siberika.idea.pascal.lang.references.resolve;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import consulo.util.lang.StringLenComparator;

import java.util.Comparator;

class UnitNameLengthComparator implements Comparator<PasEntityScope> {
    @Override
    public int compare(PasEntityScope o1, PasEntityScope o2) {
        return StringLenComparator.getInstance().compare(o2.getName(), o1.getName());
    }
}
