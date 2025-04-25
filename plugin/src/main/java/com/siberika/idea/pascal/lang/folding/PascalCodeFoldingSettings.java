package com.siberika.idea.pascal.lang.folding;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.ide.ServiceManager;

@ServiceAPI(ComponentScope.APPLICATION)
public abstract class PascalCodeFoldingSettings {
    public static PascalCodeFoldingSettings getInstance() {
        return ServiceManager.getService(PascalCodeFoldingSettings.class);
    }

    public abstract boolean isFoldWithBlocks();
    public abstract void setFoldWithBlocks(boolean value);

    public abstract boolean isCollapseEnums();
    public abstract void setCollapseEnums(boolean value);
}
