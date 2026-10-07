package consulo.object.pascal.msbuild.newProject;

import consulo.msbuild.MSBuildProcessProvider;
import consulo.object.pascal.newProject.PascalNewModuleWizardContext;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class DelphiNewModuleWizardContext extends PascalNewModuleWizardContext {
    @Nullable
    private String myBundleName;
    @Nullable
    private MSBuildProcessProvider myProvider;
    private boolean myFpcFallback = true;
    @Nullable
    private String mySdkName;

    public DelphiNewModuleWizardContext(boolean isNewProject) {
        super(isNewProject);
    }

    @Nullable
    public String getBundleName() {
        return myBundleName;
    }

    public void setBundleName(@Nullable String bundleName) {
        myBundleName = bundleName;
    }

    @Nullable
    public MSBuildProcessProvider getProvider() {
        return myProvider;
    }

    public void setProvider(@Nullable MSBuildProcessProvider provider) {
        myProvider = provider;
    }

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    public void setSdkName(@Nullable String sdkName) {
        mySdkName = sdkName;
    }

    public boolean isFpcFallback() {
        return myFpcFallback;
    }

    public void setFpcFallback(boolean fpcFallback) {
        myFpcFallback = fpcFallback;
    }
}
