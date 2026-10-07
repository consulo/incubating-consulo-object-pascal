package consulo.object.pascal.lazarus.newProject;

import consulo.annotation.access.RequiredReadAction;
import consulo.logging.Logger;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.creation.scratch.NewModuleBuilderProcessor;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.object.pascal.lazarus.externalSystem.LazarusProjectLinker;
import consulo.object.pascal.newProject.PascalProjectTemplate;
import consulo.project.Project;
import consulo.ui.ex.wizard.WizardStep;
import consulo.virtualFileSystem.VirtualFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class LazarusNewModuleBuilderProcessor implements NewModuleBuilderProcessor<LazarusNewModuleWizardContext> {
    private static final Logger LOG = Logger.getInstance(LazarusNewModuleBuilderProcessor.class);

    private static final String TEMPLATES = "project-templates/lazarus/";
    private static final String MAIN_SOURCE_EXTENSION = "lpr";
    private static final String TEMPLATE_SUFFIX = ".template";

    @Override
    public LazarusNewModuleWizardContext createContext(boolean isNewProject) {
        return new LazarusNewModuleWizardContext(isNewProject);
    }

    @Override
    public void buildSteps(Consumer<WizardStep<LazarusNewModuleWizardContext>> consumer, LazarusNewModuleWizardContext context) {
        consumer.accept(new LazarusNewModuleStep(context));
    }

    @RequiredReadAction
    @Override
    public void process(LazarusNewModuleWizardContext context, ContentEntry contentEntry, ModifiableRootModel modifiableRootModel) {
        VirtualFile projectDir = contentEntry.getFile();
        if (projectDir == null) {
            return;
        }
        String name = modifiableRootModel.getModule().getName();
        Path directory = Path.of(projectDir.getPath());
        Path projectFile = directory.resolve(name + "." + LazarusConstants.PROJECT_EXTENSION);
        String kind = context.getKind().getTemplateName();
        ClassLoader loader = getClass().getClassLoader();
        try {
            PascalProjectTemplate.write(loader, TEMPLATES + kind + "." + LazarusConstants.PROJECT_EXTENSION + TEMPLATE_SUFFIX, projectFile, name);
            PascalProjectTemplate.write(loader, TEMPLATES + kind + "." + MAIN_SOURCE_EXTENSION + TEMPLATE_SUFFIX,
                directory.resolve(name + "." + MAIN_SOURCE_EXTENSION), name);
        }
        catch (IOException e) {
            LOG.error("Cannot create Lazarus project " + projectFile, e);
            return;
        }

        Project project = modifiableRootModel.getProject();
        String sdkName = context.getSdkName();
        projectDir.refresh(true, true, () -> project.getUIAccess().give(() -> LazarusProjectLinker.linkAndImport(project, projectFile.toString(), sdkName)));
    }
}
