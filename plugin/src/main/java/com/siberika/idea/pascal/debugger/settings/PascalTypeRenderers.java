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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@State(
    name = "PascalTypeRenderersSettings",
    storages = @Storage("pascal.debugger.xml")
)
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
@Singleton
public class PascalTypeRenderers implements PersistentStateComponent<PascalTypeRenderers> {

    public List<TypeRenderer> typeRenderers = new ArrayList<>();

    public static PascalTypeRenderers getInstance() {
        return ServiceManager.getService(PascalTypeRenderers.class);
    }

    @Override
    public void loadState(@NotNull PascalTypeRenderers state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public PascalTypeRenderers getState() {
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
        PascalTypeRenderers that = (PascalTypeRenderers) o;
        return Objects.equals(typeRenderers, that.typeRenderers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeRenderers);
    }
}
