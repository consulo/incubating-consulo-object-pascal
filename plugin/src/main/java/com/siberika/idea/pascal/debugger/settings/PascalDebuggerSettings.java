package com.siberika.idea.pascal.debugger.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.Configurable;
import consulo.execution.debug.setting.DebuggerSettingsCategory;
import consulo.execution.debug.setting.XDebuggerSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ExtensionImpl
public class PascalDebuggerSettings extends XDebuggerSettings<Element> {
    public PascalDebuggerSettings() {
        super("pascal");
    }

    @NotNull
    @Override
    public Collection<? extends Configurable> createConfigurables(@NotNull DebuggerSettingsCategory category) {
        switch (category) {
            case DATA_VIEWS:
                return createDataViewsConfigurable();
        }
        return Collections.emptyList();
    }

    @NotNull
    public static List<Configurable> createDataViewsConfigurable() {
        return Arrays.asList(new DataViewsConfigurable("debug.settings.general."), new TypeRenderersConfigurable());
    }

    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element state) {
    }
}
