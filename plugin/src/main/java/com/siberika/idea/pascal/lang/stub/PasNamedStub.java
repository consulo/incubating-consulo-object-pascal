package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import consulo.language.psi.stub.StubElement;

public interface PasNamedStub<T extends PascalNamedElement> extends StubElement<T> {
    String getName();

    PasField.FieldType getType();

    String getUniqueName();

    String getContainingUnitName();

    boolean isExported();

    int getFlags();
}
