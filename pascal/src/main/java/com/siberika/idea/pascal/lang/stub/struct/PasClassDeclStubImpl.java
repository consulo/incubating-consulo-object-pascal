package com.siberika.idea.pascal.lang.stub.struct;

import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import consulo.language.psi.stub.StubElement;

import java.util.List;

public class PasClassDeclStubImpl extends PasStructStubImpl<PascalClassDecl> implements PasClassDeclStub {
    public PasClassDeclStubImpl(StubElement parent, String name, String containingUnitName,
                                List<String> parentNames, List<String> aliases, PasClassDeclStubElementType stubElementType, List<String> typeParameters) {
        super(parent, name, containingUnitName, parentNames, aliases, stubElementType, typeParameters);
    }
}
