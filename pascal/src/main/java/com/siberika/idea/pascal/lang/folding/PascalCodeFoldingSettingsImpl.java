package com.siberika.idea.pascal.lang.folding;

import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "PascalCodeFoldingSettings", storages = @Storage("editor.codeinsight.xml"))
@ServiceImpl
@Singleton
public class PascalCodeFoldingSettingsImpl extends PascalCodeFoldingSettings implements PersistentStateComponent<PascalCodeFoldingSettingsImpl> {

    private boolean FOLD_WITH_BLOCKS = false;
    private boolean COLLAPSE_ENUMS = true;

    @Nullable
    @Override
    public PascalCodeFoldingSettingsImpl getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PascalCodeFoldingSettingsImpl state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    @Override
    public boolean isFoldWithBlocks() {
        return FOLD_WITH_BLOCKS;
    }

    @Override
    public void setFoldWithBlocks(boolean value) {
        FOLD_WITH_BLOCKS = value;
    }

    @Override
    public boolean isCollapseEnums() {
        return COLLAPSE_ENUMS;
    }

    @Override
    public void setCollapseEnums(boolean value) {
        COLLAPSE_ENUMS = value;
    }
}
