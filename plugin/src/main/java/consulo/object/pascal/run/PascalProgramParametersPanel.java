package consulo.object.pascal.run;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.siberika.idea.pascal.run.PascalRunConfigurationParams;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 12/07/2021
 */
public class PascalProgramParametersPanel extends CommonProgramParametersPanel {
    private final Project myProject;
    private ModulesComboBox myModulesComboBox;
    private LabeledComponent<ModulesComboBox> myModuleLabel;

    public PascalProgramParametersPanel(Project project) {
       super(false);
       myProject = project;

       init();
   }

    @Override
    public void applyTo(CommonProgramRunConfigurationParameters configuration) {
        super.applyTo(configuration);

        PascalRunConfigurationParams params = (PascalRunConfigurationParams) configuration;

        Module selectedModule = myModulesComboBox.getSelectedModule();
        params.setModuleName(selectedModule == null ? null : selectedModule.getName());
    }

    @Override
    public void reset(CommonProgramRunConfigurationParameters configuration) {
        super.reset(configuration);

        PascalRunConfigurationParams params = (PascalRunConfigurationParams) configuration;

        String moduleName = params.getModuleName();
        Module module = moduleName != null ? ModuleManager.getInstance(myProject).findModuleByName(moduleName) : null;
        myModulesComboBox.setSelectedModule(module);
    }

    @Override
    protected void addComponents() {
        super.addComponents();

        myModulesComboBox = new ModulesComboBox();
        myModulesComboBox.fillModules(myProject);

        myModuleLabel = LabeledComponent.create(myModulesComboBox, "Module");

        add(myModuleLabel);
    }

    @Override
    public void setAnchor(JComponent anchor) {
        super.setAnchor(anchor);
        myModuleLabel.setAnchor(anchor);
    }
}
