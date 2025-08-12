package com.siberika.idea.pascal.debugger.settings;

import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DataViewsConfigurable extends AbstractConfigurable<PascalDebuggerViewSettings> {

    public DataViewsConfigurable(String bundlePrefix) {
        super(bundlePrefix);
    }

    @Override
    public LocalizeValue getDisplayName() {
        return ObjectPascalLocalize.debugSettingsGeneral();
    }

    @Override
    public JComponent createComponent(Disposable disposable) {
        return createOptionsPanel(PascalDebuggerViewSettings.class);
    }

    @Override
    public void apply() {
        doApply(PascalDebuggerViewSettings.getInstance());
    }

    @Override
    public void reset() {
        doReset(PascalDebuggerViewSettings.getInstance());
    }

    @Override
    public boolean isModified() {
        try {
            return !PascalDebuggerViewSettings.getInstance().equals(doApply(new PascalDebuggerViewSettings()));
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    @NotNull
    public String getHelpTopic() {
        return "Debugger_Pascal_Data_Views";
    }

    @Override
    @NotNull
    public String getId() {
        return getHelpTopic();
    }

}
