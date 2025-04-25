package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasInterfaceDeclStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

public abstract class PascalInterfaceDeclImpl extends PasStubStructTypeImpl<PascalInterfaceDecl, PasInterfaceDeclStub> implements PascalInterfaceDecl {

    public PascalInterfaceDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalInterfaceDeclImpl(PasInterfaceDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

}
