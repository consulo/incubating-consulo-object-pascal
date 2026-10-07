package consulo.object.pascal.lazarus.setting;

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusExecutionSettings extends ExternalSystemExecutionSettings {
    private final String myProjectFile;
    @Nullable
    private final String myBuildMode;
    @Nullable
    private final String mySdkName;

    public LazarusExecutionSettings(String projectFile, @Nullable String buildMode, @Nullable String sdkName) {
        myProjectFile = projectFile;
        myBuildMode = buildMode;
        mySdkName = sdkName;
    }

    public String getProjectFile() {
        return myProjectFile;
    }

    @Nullable
    public String getBuildMode() {
        return myBuildMode;
    }

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }
        LazarusExecutionSettings that = (LazarusExecutionSettings) o;
        return myProjectFile.equals(that.myProjectFile)
            && Objects.equals(myBuildMode, that.myBuildMode)
            && Objects.equals(mySdkName, that.mySdkName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), myProjectFile, myBuildMode, mySdkName);
    }
}
