package consulo.object.pascal.msbuild.newProject;

import com.siberika.idea.pascal.sdk.DelphiSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.application.Application;
import consulo.disposer.Disposable;
import consulo.module.ui.BundleBox;
import consulo.msbuild.importProvider.MSBuildBundleChooser;
import consulo.object.pascal.msbuild.localize.DelphiLocalize;
import consulo.object.pascal.newProject.PascalNewModuleStep;
import consulo.ui.CheckBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class DelphiNewModuleStep extends PascalNewModuleStep<DelphiNewModuleWizardContext> {
    private MSBuildBundleChooser myBundleChooser;
    private CheckBox myFpcFallbackBox;
    private BundleBox mySdkBox;

    public DelphiNewModuleStep(DelphiNewModuleWizardContext context) {
        super(context);
    }

    @RequiredUIAccess
    @Override
    protected void extend(FormBuilder builder, Disposable uiDisposable) {
        super.extend(builder, uiDisposable);
        myBundleChooser = new MSBuildBundleChooser(Application.get(), uiDisposable, null);
        builder.addLabeled(DelphiLocalize.newProjectMsbuildLabel(), myBundleChooser.getComponent());
        myFpcFallbackBox = CheckBox.create(DelphiLocalize.newProjectFpcFallback());
        myFpcFallbackBox.setValue(true);
        builder.addLabeled(DelphiLocalize.newProjectFpcLabel(), myFpcFallbackBox);
        mySdkBox = addSdkBox(builder, uiDisposable, DelphiSdkType.getInstance(), FPCSdkType.getInstance());
    }

    @Override
    public void onStepLeave(DelphiNewModuleWizardContext context) {
        super.onStepLeave(context);
        if (myBundleChooser != null) {
            context.setBundleName(myBundleChooser.getSelectedBundleName());
            context.setProvider(myBundleChooser.getSelectedProvider());
        }
        if (myFpcFallbackBox != null) {
            context.setFpcFallback(myFpcFallbackBox.getValueOrError());
        }
        context.setSdkName(mySdkBox != null ? mySdkBox.getSelectedBundleName() : null);
    }
}
