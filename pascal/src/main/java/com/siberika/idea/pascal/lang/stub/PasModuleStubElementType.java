package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PasModuleImpl;
import consulo.language.ast.LighterAST;
import consulo.language.ast.LighterASTNode;
import consulo.language.psi.stub.*;
import consulo.util.collection.SmartList;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasModuleStubElementType extends ILightStubElementType<PasModuleStub, PascalModule> {

    public static PasModuleStubElementType INSTANCE;

    public PasModuleStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public PasModuleStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasModuleStubImpl(parentStub, "-", null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public PascalModule createPsi(@NotNull PasModuleStub stub) {
        return new PasModuleImpl(stub, this);
    }

    @NotNull
    @Override
    public PasModuleStub createStub(@NotNull PascalModule psi, StubElement parentStub) {
        return new PasModuleStubImpl(parentStub, psi.getName(), psi.getModuleType(), psi.getUsedUnitsPublic(), psi.getUsedUnitsPrivate(),
            psi.getIncludesPublic(), psi.getIncludesPrivate());
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.module";
    }

    @Override
    public void serialize(@NotNull PasModuleStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.printStub("PasModuleStub.serialize", stub);

        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getModuleType().name());
        StubUtil.writeStringCollection(dataStream, stub.getUsedUnitsPublic());
        StubUtil.writeStringCollection(dataStream, stub.getUsedUnitsPrivate());
        StubUtil.writeStringCollection(dataStream, stub.getIncludesPublic());
        StubUtil.writeStringCollection(dataStream, stub.getIncludesPrivate());
    }

    @NotNull
    @Override
    public PasModuleStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StubUtil.readName(dataStream);
        PascalModule.ModuleType type = StubUtil.readEnum(dataStream, PascalModule.ModuleType.class);
        List<String> usedUnitsPublic = new SmartList<>();
        StubUtil.readStringCollection(dataStream, usedUnitsPublic);
        List<String> usedUnitsPrivate = new SmartList<>();
        StubUtil.readStringCollection(dataStream, usedUnitsPrivate);
        List<String> includesPublic = new SmartList<>();
        StubUtil.readStringCollection(dataStream, includesPublic);
        List<String> includesPrivate = new SmartList<>();
        StubUtil.readStringCollection(dataStream, includesPrivate);
        return new PasModuleStubImpl(parentStub, name, type, usedUnitsPublic, usedUnitsPrivate, includesPublic, includesPrivate);
    }

    @Override
    public void indexStub(@NotNull PasModuleStub stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalModuleIndex.KEY, stub.getName().toUpperCase());
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
        sink.occurrence(PascalUnitSymbolIndex.KEY, stub.getName().toUpperCase());
    }
}
