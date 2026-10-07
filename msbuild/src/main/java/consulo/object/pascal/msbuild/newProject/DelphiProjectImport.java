package consulo.object.pascal.msbuild.newProject;

import consulo.application.WriteAction;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.ModuleWithNameAlreadyExistsException;
import consulo.msbuild.MSBuildProjectImporter;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
final class DelphiProjectImport {
    private static final String ROOT_MODULE_SUFFIX = " (Root)";

    private DelphiProjectImport() {
    }

    @RequiredUIAccess
    static void reimport(Project project, Module module, String projectName) {
        if (project.isDisposed() || module.isDisposed()) {
            return;
        }
        String rootName = projectName + ROOT_MODULE_SUFFIX;
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        if (!module.getName().equals(rootName) && moduleManager.findModuleByName(rootName) == null) {
            ModifiableModuleModel modifiableModel = moduleManager.getModifiableModel();
            try {
                modifiableModel.renameModule(module, rootName);
            }
            catch (ModuleWithNameAlreadyExistsException e) {
                modifiableModel.dispose();
                return;
            }
            WriteAction.runAndWait(modifiableModel::commit);
        }
        MSBuildProjectImporter.getInstance(project).reimport();
    }
}
