package consulo.object.pascal.newProject;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTypeId;
import consulo.disposer.Disposable;
import consulo.module.creation.ui.UnifiedProjectOrModuleNameStep;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.object.pascal.sdk.PascalModuleSdkUtil;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class PascalNewModuleStep<C extends PascalNewModuleWizardContext> extends UnifiedProjectOrModuleNameStep<C> {
    private ComboBox<PascalProjectKind> myKindBox;

    public PascalNewModuleStep(C context) {
        super(context);
    }

    @RequiredUIAccess
    @Override
    protected void extend(FormBuilder builder, Disposable uiDisposable) {
        super.extend(builder, uiDisposable);
        myKindBox = ComboBox.create(PascalProjectKind.values());
        myKindBox.setTextRenderer(kind -> kind == null ? ObjectPascalLocalize.newProjectKindApplication() : kind.getDisplayName());
        myKindBox.setValue(PascalProjectKind.APPLICATION);
        builder.addLabeled(ObjectPascalLocalize.newProjectKindLabel(), myKindBox);
    }

    @RequiredUIAccess
    protected BundleBox addSdkBox(FormBuilder builder, Disposable uiDisposable, SdkTypeId... sdkTypes) {
        Set<SdkTypeId> types = Set.of(sdkTypes);
        BundleBox sdkBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilter(types::contains)
            .build();
        Sdk sdk = PascalModuleSdkUtil.findDefaultSdk(sdkTypes);
        if (sdk != null) {
            sdkBox.setSelectedBundle(sdk.getName());
        }
        builder.addLabeled(ObjectPascalLocalize.newProjectSdkLabel(), sdkBox.getComponent());
        return sdkBox;
    }

    @Override
    public void onStepLeave(C context) {
        super.onStepLeave(context);
        PascalProjectKind kind = myKindBox != null ? myKindBox.getValue() : null;
        context.setKind(kind != null ? kind : PascalProjectKind.APPLICATION);
    }
}
