package consulo.object.pascal.module.extension;

import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27/06/2021
 */
public class PascalMutableModuleExtension extends PascalModuleExtension implements ObjectPascalMutableModuleExtension<PascalModuleExtension> {
    public PascalMutableModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk() {
        return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
    }

    @RequiredUIAccess
    @Nullable
    @Override
    public Component createConfigurationComponent(Disposable uiDisposable, Runnable updateOnCheck) {
        VerticalLayout root = VerticalLayout.create();
        root.add(ModuleExtensionBundleBoxBuilder.createAndDefine(this, uiDisposable, updateOnCheck).build());
        return root;
    }

    @Override
    public void setEnabled(boolean enabled) {
        myIsEnabled = enabled;
    }

    @Override
    public boolean isModified(PascalModuleExtension originalExtension) {
        return isModifiedImpl(originalExtension);
    }
}
