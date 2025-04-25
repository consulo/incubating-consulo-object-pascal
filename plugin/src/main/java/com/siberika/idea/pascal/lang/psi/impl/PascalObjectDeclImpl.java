package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.psi.PascalObjectDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasObjectDeclStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

public abstract class PascalObjectDeclImpl extends PasStubStructTypeImpl<PascalObjectDecl, PasObjectDeclStub> implements PascalObjectDecl {

    public PascalObjectDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalObjectDeclImpl(PasObjectDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

}
