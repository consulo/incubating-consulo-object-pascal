package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.field.Flag;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;

public class PasNamedStubBase<T extends PsiElement> extends StubBase<T> {

    protected String name;
    protected String uniqueName;
    protected String containingUnitName;

    volatile protected int flags;

    protected PasNamedStubBase(StubElement parent, IStubElementType elementType, String name, String containingUnitName) {
        super(parent, elementType);
        this.name = name;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "") + name;
        this.containingUnitName = containingUnitName;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getContainingUnitName() {
        return containingUnitName;
    }

    public boolean isFlagSet(Flag flag) {
        return (flags & (1 << flag.ordinal())) != 0;
    }

    public void setFlag(Flag flag, boolean value) {
        //TODO: make atomic
        int f = flags | (1 << flag.ordinal());
        flags = f & (~((value ? 0 : 1) << flag.ordinal()));

    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

}
