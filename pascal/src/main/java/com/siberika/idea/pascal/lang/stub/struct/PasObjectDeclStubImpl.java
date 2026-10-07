package com.siberika.idea.pascal.lang.stub.struct;

import com.siberika.idea.pascal.lang.psi.PascalObjectDecl;
import consulo.language.psi.stub.StubElement;

import java.util.List;

public class PasObjectDeclStubImpl extends PasStructStubImpl<PascalObjectDecl> implements PasObjectDeclStub {
    public PasObjectDeclStubImpl(StubElement parent, String name, String containingUnitName,
                                 List<String> parentNames, List<String> aliases, PasObjectDeclStubElementType stubElementType, List<String> typeParameters) {
        super(parent, name, containingUnitName, parentNames, aliases, stubElementType, typeParameters);
    }
}
