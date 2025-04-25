package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.util.lang.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/08/2015
 */
public interface PascalModule extends PasEntityScope, PascalStubElement<PasModuleStub> {

    enum ModuleType {
        UNIT, PROGRAM, LIBRARY, PACKAGE
    }

    ModuleType getModuleType();

    @Nullable
    PasField getPublicField(final String name);

    @Nullable
    PasField getPrivateField(final String name);

    @NotNull
    Collection<PasField> getPrivateFields();

    @NotNull
    Collection<PasField> getPublicFields();

    List<SmartPsiElementPointer<PasEntityScope>> getPrivateUnits();

    List<SmartPsiElementPointer<PasEntityScope>> getPublicUnits();

    // Used in interface and implementation identifiers list
    Pair<List<PascalNamedElement>, List<PascalNamedElement>> getIdentsFrom(@Nullable String module, boolean includeInterface, List<String> unitPrefixes);

    @NotNull
    List<String> getUsedUnitsPublic();

    @NotNull
    List<String> getUsedUnitsPrivate();

    @Nullable
    PascalRoutine getPublicRoutine(final String reducedName);

    @Nullable
    PascalRoutine getPrivateRoutine(final String reducedName);

}
