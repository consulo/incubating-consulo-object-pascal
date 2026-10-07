package consulo.object.pascal.lazarus.setting;

import consulo.externalSystem.setting.ExternalProjectSettings;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusProjectSettings extends ExternalProjectSettings {
    @Nullable
    private String myBuildMode;
    @Nullable
    private String mySdkName;

    @Nullable
    public String getBuildMode() {
        return myBuildMode;
    }

    public void setBuildMode(@Nullable String buildMode) {
        myBuildMode = buildMode;
    }

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    public void setSdkName(@Nullable String sdkName) {
        mySdkName = sdkName;
    }

    @Override
    public LazarusProjectSettings clone() {
        LazarusProjectSettings clone = new LazarusProjectSettings();
        copyTo(clone);
        clone.myBuildMode = myBuildMode;
        clone.mySdkName = mySdkName;
        return clone;
    }
}
