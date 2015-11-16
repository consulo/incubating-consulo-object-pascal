package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 15/09/2013
 */
public interface PasEntityScope extends PascalNamedElement {
    @NotNull
    String getName();
    @Nullable
    PasField getField(final String name);
    @NotNull
    Collection<PasField> getAllFields();

    // owning class for methods, parent classes/interfaces for structured types
    @NotNull
    List<SmartPsiElementPointer<PasEntityScope>> getParentScope();

    /**
     * For methods and method implementations returns containing class
     * For routines returns containing module
     * For nested routines returns containing routine
     * For structured types returns containing module
     * For nested structured types returns containing type
     * For modules returns null
     */
    @Nullable
    PasEntityScope getContainingScope();

    String getKey();

}
