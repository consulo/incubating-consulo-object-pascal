package consulo.object.pascal.lazarus.externalSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.ExternalSystemManager;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.object.pascal.lazarus.setting.LazarusExecutionSettings;
import consulo.object.pascal.lazarus.setting.LazarusLocalSettings;
import consulo.object.pascal.lazarus.setting.LazarusProjectSettings;
import consulo.object.pascal.lazarus.setting.LazarusSettings;
import consulo.object.pascal.lazarus.setting.LazarusSettingsListener;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusManager implements ExternalSystemAutoImportAware,
    ExternalSystemManager<LazarusProjectSettings, LazarusSettingsListener, LazarusSettings, LazarusLocalSettings, LazarusExecutionSettings> {

    private final ProcessHandlerBuilderFactory myProcessHandlerBuilderFactory;
    private final LazarusAutoImportAware myAutoImportAware = new LazarusAutoImportAware();
    private final ExternalSystemAutoImportAware myAutoImportDelegate = new CachingExternalSystemAutoImportAware(myAutoImportAware);

    @Inject
    public LazarusManager(ProcessHandlerBuilderFactory processHandlerBuilderFactory) {
        myProcessHandlerBuilderFactory = processHandlerBuilderFactory;
    }

    @Override
    public ProjectSystemId getSystemId() {
        return LazarusConstants.SYSTEM_ID;
    }

    @Override
    public Function<Project, LazarusSettings> getSettingsProvider() {
        return LazarusSettings::getInstance;
    }

    @Override
    public Function<Project, LazarusLocalSettings> getLocalSettingsProvider() {
        return LazarusLocalSettings::getInstance;
    }

    @Override
    public Function<Pair<Project, String>, LazarusExecutionSettings> getExecutionSettingsProvider() {
        return pair -> {
            String projectPath = pair.getSecond();
            LazarusProjectSettings settings = LazarusSettings.getInstance(pair.getFirst()).getLinkedProjectSettings(projectPath);
            return new LazarusExecutionSettings(projectPath,
                settings != null ? settings.getBuildMode() : null,
                settings != null ? settings.getSdkName() : null);
        };
    }

    @Override
    public Supplier<? extends ExternalSystemProjectResolver<LazarusExecutionSettings>> getProjectResolverFactory() {
        return LazarusProjectResolver::new;
    }

    @Override
    public Supplier<? extends ExternalSystemTaskManager<LazarusExecutionSettings>> getTaskManagerFactory() {
        return () -> new LazarusTaskManager(myProcessHandlerBuilderFactory);
    }

    @Override
    public FileChooserDescriptor getExternalProjectDescriptor() {
        return new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return file.isDirectory() || LazarusConstants.PROJECT_EXTENSION.equalsIgnoreCase(file.getExtension());
            }
        };
    }

    @Nullable
    @Override
    public String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        return myAutoImportDelegate.getAffectedExternalProjectPath(changedFileOrDirPath, project);
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        return myAutoImportAware.getAffectedExternalProjectFilePaths(projectPath, project);
    }
}
