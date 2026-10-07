package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import consulo.language.psi.stub.StubElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasModuleStubImpl extends PasNamedStubBase<PascalModule> implements PasModuleStub {

    private PascalModule.ModuleType moduleType;
    private List<String> usedUnitsPublic;
    private List<String> usedUnitsPrivate;
    private List<String> includesPublic;
    private List<String> includesPrivate;

    public PasModuleStubImpl(StubElement parent, String name, PascalModule.ModuleType moduleType,
                             List<String> usedUnitsPublic, List<String> usedUnitsPrivate,
                             List<String> includesPublic, List<String> includesPrivate) {
        super(parent, PasModuleStubElementType.INSTANCE, name, name);
        this.moduleType = moduleType;
        this.usedUnitsPublic = usedUnitsPublic;
        this.usedUnitsPrivate = usedUnitsPrivate;
        this.includesPublic = includesPublic;
        this.includesPrivate = includesPrivate;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.UNIT;
    }

    @Override
    public boolean isExported() {
        return true;
    }

    @NotNull
    @Override
    public PascalModule.ModuleType getModuleType() {
        return moduleType;
    }

    @NotNull
    @Override
    public List<String> getUsedUnitsPublic() {
        return usedUnitsPublic;
    }

    @NotNull
    @Override
    public List<String> getUsedUnitsPrivate() {
        return usedUnitsPrivate;
    }

    @NotNull
    @Override
    public List<String> getIncludesPublic() {
        return includesPublic;
    }

    @NotNull
    @Override
    public List<String> getIncludesPrivate() {
        return includesPrivate;
    }
}
