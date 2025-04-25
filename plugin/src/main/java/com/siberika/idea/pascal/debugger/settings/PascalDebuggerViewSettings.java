package com.siberika.idea.pascal.debugger.settings;


import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.ide.ServiceManager;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@State(
    name = "PascalDebuggerViewsSettings",
    storages = @Storage("pascal.debugger.xml")
)
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
@Singleton
public class PascalDebuggerViewSettings implements PersistentStateComponent<PascalDebuggerViewSettings> {

    public boolean showNonPrintable = true;
    public boolean refineStrings = true;
    public boolean refineDynamicArrays = true;
    public boolean refineOpenArrays = true;
    public boolean refineStructured = true;
    public int limitChars = 1000;
    public int limitElements = 1000;
    public int limitChilds = 100;
    public int limitValueSize = 2 * 1024 * 1024;

    public static PascalDebuggerViewSettings getInstance() {
        return ServiceManager.getService(PascalDebuggerViewSettings.class);
    }

    @Override
    public void loadState(@NotNull PascalDebuggerViewSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public PascalDebuggerViewSettings getState() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PascalDebuggerViewSettings that = (PascalDebuggerViewSettings) o;
        return showNonPrintable == that.showNonPrintable &&
            refineStrings == that.refineStrings &&
            refineDynamicArrays == that.refineDynamicArrays &&
            refineOpenArrays == that.refineOpenArrays &&
            refineStructured == that.refineStructured &&
            limitChars == that.limitChars &&
            limitElements == that.limitElements &&
            limitChilds == that.limitChilds &&
            limitValueSize == that.limitValueSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(showNonPrintable, refineStrings, refineDynamicArrays, refineOpenArrays, refineStructured, limitChars, limitElements, limitChilds, limitValueSize);
    }
}
