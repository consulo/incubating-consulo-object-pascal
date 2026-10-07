package consulo.object.pascal.lazarus.externalSystem;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ExternalSystemDataKeys;
import consulo.externalSystem.model.task.ProgressExecutionMode;
import consulo.externalSystem.service.project.ExternalProjectRefreshCallback;
import consulo.externalSystem.service.project.ExternalSystemProjectRefresher;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.content.ProjectRootManager;
import consulo.object.pascal.lazarus.setting.LazarusProjectSettings;
import consulo.object.pascal.lazarus.setting.LazarusSettings;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nullable;

import java.util.Collections;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public final class LazarusProjectLinker {
    private LazarusProjectLinker() {
    }

    @RequiredUIAccess
    public static void linkAndImport(Project project, String projectFilePath, @Nullable String sdkName) {
        if (project.isDisposed()) {
            return;
        }
        String path = ExternalSystemApiUtil.toCanonicalPath(projectFilePath);
        LazarusSettings settings = LazarusSettings.getInstance(project);
        if (settings.getLinkedProjectSettings(path) == null) {
            LazarusProjectSettings projectSettings = new LazarusProjectSettings();
            projectSettings.setExternalProjectPath(path);
            projectSettings.setSdkName(sdkName);
            settings.linkProject(projectSettings);
        }
        project.putUserData(ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT, Boolean.TRUE);
        ExternalSystemProjectRefresher.getInstance().refreshProject(project, LazarusConstants.SYSTEM_ID, path, new ExternalProjectRefreshCallback() {
            @Override
            public void onSuccess(@Nullable DataNode<ProjectData> externalProject) {
                if (externalProject == null) {
                    return;
                }
                ExternalSystemApiUtil.executeProjectChangeAction(true, new DisposeAwareProjectChange(project) {
                    @RequiredUIAccess
                    @Override
                    public void execute() {
                        ProjectDataManager projectDataManager = project.getApplication().getInstance(ProjectDataManager.class);
                        ProjectRootManager.getInstance(project).mergeRootsChangesDuring(
                            () -> projectDataManager.importData(externalProject.getKey(), Collections.singleton(externalProject), project, true)
                        );
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage, @Nullable String errorDetails) {
            }
        }, false, ProgressExecutionMode.IN_BACKGROUND_ASYNC);
    }
}
