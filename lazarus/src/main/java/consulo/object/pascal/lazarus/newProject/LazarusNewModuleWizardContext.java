package consulo.object.pascal.lazarus.newProject;

import consulo.object.pascal.newProject.PascalNewModuleWizardContext;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class LazarusNewModuleWizardContext extends PascalNewModuleWizardContext {
    @Nullable
    private String mySdkName;

    public LazarusNewModuleWizardContext(boolean isNewProject) {
        super(isNewProject);
    }

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    public void setSdkName(@Nullable String sdkName) {
        mySdkName = sdkName;
    }
}
