package consulo.object.pascal.lazarus.externalSystem;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusModuleData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<LazarusModuleData> KEY = Key.create(LazarusModuleData.class, ProjectKeys.MODULE.getProcessingWeight() + 1);

    private final String myProjectFile;
    @Nullable
    private final String myMainFile;
    @Nullable
    private final String myBuildMode;
    @Nullable
    private final String myTargetFile;
    @Nullable
    private final String mySdkName;
    @Nullable
    private final String mySdkOwnerModuleName;

    public LazarusModuleData(String projectFile,
                             @Nullable String mainFile,
                             @Nullable String buildMode,
                             @Nullable String targetFile,
                             @Nullable String sdkName,
                             @Nullable String sdkOwnerModuleName) {
        super(LazarusConstants.SYSTEM_ID);
        myProjectFile = projectFile;
        myMainFile = mainFile;
        myBuildMode = buildMode;
        myTargetFile = targetFile;
        mySdkName = sdkName;
        mySdkOwnerModuleName = sdkOwnerModuleName;
    }

    public String getProjectFile() {
        return myProjectFile;
    }

    @Nullable
    public String getMainFile() {
        return myMainFile;
    }

    @Nullable
    public String getBuildMode() {
        return myBuildMode;
    }

    @Nullable
    public String getTargetFile() {
        return myTargetFile;
    }

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    @Nullable
    public String getSdkOwnerModuleName() {
        return mySdkOwnerModuleName;
    }
}
