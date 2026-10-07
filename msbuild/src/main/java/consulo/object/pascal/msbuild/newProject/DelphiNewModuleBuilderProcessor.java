package consulo.object.pascal.msbuild.newProject;

import consulo.annotation.access.RequiredReadAction;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.creation.scratch.NewModuleBuilderProcessor;
import consulo.msbuild.MSBuildProcessProvider;
import consulo.msbuild.module.extension.MSBuildSolutionMutableModuleExtension;
import consulo.object.pascal.msbuild.DelphiProjectCapability;
import consulo.object.pascal.msbuild.DelphiProjectFile;
import consulo.object.pascal.newProject.PascalProjectTemplate;
import consulo.project.Project;
import consulo.ui.ex.wizard.WizardStep;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class DelphiNewModuleBuilderProcessor implements NewModuleBuilderProcessor<DelphiNewModuleWizardContext> {
    private static final Logger LOG = Logger.getInstance(DelphiNewModuleBuilderProcessor.class);

    private static final String TEMPLATES = "project-templates/delphi/";
    private static final String TEMPLATE_SUFFIX = ".template";
    private static final String MAIN_SOURCE_EXTENSION = "dpr";
    private static final String FPC_TARGETS = "Delphi.FPC.targets";
    private static final String FPC_IMPORT = "    <Import Project=\"" + FPC_TARGETS + "\" Condition=\"!Exists('$(BDS)\\Bin\\CodeGear.Delphi.Targets') and Exists('"
        + FPC_TARGETS + "')\"/>\n";

    @Override
    public DelphiNewModuleWizardContext createContext(boolean isNewProject) {
        return new DelphiNewModuleWizardContext(isNewProject);
    }

    @Override
    public void buildSteps(Consumer<WizardStep<DelphiNewModuleWizardContext>> consumer, DelphiNewModuleWizardContext context) {
        consumer.accept(new DelphiNewModuleStep(context));
    }

    @RequiredReadAction
    @Override
    public void process(DelphiNewModuleWizardContext context, ContentEntry contentEntry, ModifiableRootModel modifiableRootModel) {
        VirtualFile projectDir = contentEntry.getFile();
        if (projectDir == null) {
            return;
        }
        Module module = modifiableRootModel.getModule();
        String name = module.getName();
        Path directory = Path.of(projectDir.getPath());
        Path projectFile = directory.resolve(name + "." + DelphiProjectFile.EXTENSION);
        String kind = context.getKind().getTemplateName();
        Map<String, String> variables = Map.of(
            "GUID", UUID.randomUUID().toString().toUpperCase(Locale.ROOT),
            "FPC_IMPORT", context.isFpcFallback() ? FPC_IMPORT : ""
        );
        ClassLoader loader = getClass().getClassLoader();
        try {
            PascalProjectTemplate.write(loader, TEMPLATES + kind + "." + DelphiProjectFile.EXTENSION + TEMPLATE_SUFFIX, projectFile, name, variables);
            PascalProjectTemplate.write(loader, TEMPLATES + kind + "." + MAIN_SOURCE_EXTENSION + TEMPLATE_SUFFIX,
                directory.resolve(name + "." + MAIN_SOURCE_EXTENSION), name, variables);
            if (context.isFpcFallback()) {
                PascalProjectTemplate.copy(loader, TEMPLATES + FPC_TARGETS, directory.resolve(FPC_TARGETS));
            }
        }
        catch (IOException e) {
            LOG.error("Cannot create Delphi project " + projectFile, e);
            return;
        }

        MSBuildProcessProvider provider = context.getProvider();
        String bundleName = context.getBundleName();
        if (provider == null || bundleName == null) {
            return;
        }
        MSBuildSolutionMutableModuleExtension<?> solution = modifiableRootModel.getExtensionWithoutCheck(provider.getSolutionModuleExtensionId());
        if (solution == null) {
            return;
        }
        solution.setEnabled(true);
        solution.setProjectFileUrl(VirtualFileUtil.pathToUrl(projectFile.toString()));
        solution.setSdkName(bundleName);
        solution.setProcessProviderId(provider.getId());

        Project project = modifiableRootModel.getProject();
        project.putUserData(DelphiProjectCapability.NEW_PROJECT_SDK_NAME, context.getSdkName());
        projectDir.refresh(true, true, () -> project.getUIAccess().give(() -> DelphiProjectImport.reimport(project, module, name)));
    }
}
