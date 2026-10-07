package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasRecordDeclStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

public abstract class PascalRecordDeclImpl extends PasStubStructTypeImpl<PascalRecordDecl, PasRecordDeclStub> implements PascalRecordDecl {

    public PascalRecordDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalRecordDeclImpl(PasRecordDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

}
