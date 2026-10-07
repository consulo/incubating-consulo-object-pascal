package com.siberika.idea.pascal.run;

import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.object.pascal.run.PascalProgramParametersPanel;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;

public class PascalRunConfigurationEditor extends SettingsEditor<PascalRunConfiguration> {
    private final PascalProgramParametersPanel myPanel;

    public PascalRunConfigurationEditor(Project project) {
        myPanel = new PascalProgramParametersPanel(project);
    }

    @Override
    @RequiredUIAccess
    protected void resetEditorFrom(PascalRunConfiguration runConfiguration) {
        myPanel.reset(runConfiguration);
    }

    @Override
    @RequiredUIAccess
    protected void applyEditorTo(PascalRunConfiguration runConfiguration) throws ConfigurationException {
        myPanel.apply(runConfiguration);
    }

    @Override
    @RequiredUIAccess
    protected Component createUIComponent() {
        myPanel.build();
        return myPanel.getComponent();
    }
}
