package com.siberika.idea.pascal.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import consulo.object.pascal.run.PascalProgramParametersPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 06/01/2013
 */
public class PascalRunConfigurationEditor extends SettingsEditor<PascalRunConfiguration> {
    private final Project myProject;
    private PascalProgramParametersPanel myPanel;

    public PascalRunConfigurationEditor(Project project) {
        myProject = project;
    }

    @Override
    protected void resetEditorFrom(PascalRunConfiguration runConfiguration) {
        myPanel.reset(runConfiguration);
    }

    @Override
    protected void applyEditorTo(PascalRunConfiguration runConfiguration) throws ConfigurationException {
        myPanel.applyTo(runConfiguration);
    }

    @Override
    @NotNull
    protected JComponent createEditor() {
        if (myPanel == null) {
            myPanel = new PascalProgramParametersPanel(myProject);
        }
        return myPanel;
    }

    @Override
    protected void disposeEditor() {
        myPanel = null;
    }
}