package consulo.object.pascal.lazarus.newProject;

import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.disposer.Disposable;
import consulo.module.ui.BundleBox;
import consulo.object.pascal.newProject.PascalNewModuleStep;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class LazarusNewModuleStep extends PascalNewModuleStep<LazarusNewModuleWizardContext> {
    private BundleBox mySdkBox;

    public LazarusNewModuleStep(LazarusNewModuleWizardContext context) {
        super(context);
    }

    @RequiredUIAccess
    @Override
    protected void extend(FormBuilder builder, Disposable uiDisposable) {
        super.extend(builder, uiDisposable);
        mySdkBox = addSdkBox(builder, uiDisposable, FPCSdkType.getInstance());
    }

    @Override
    public void onStepLeave(LazarusNewModuleWizardContext context) {
        super.onStepLeave(context);
        context.setSdkName(mySdkBox != null ? mySdkBox.getSelectedBundleName() : null);
    }
}
