package consulo.object.pascal.module.extension.impl;

import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.object.pascal.module.extension.ObjectPascalMutableModuleExtension;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27/06/2021
 */
public class ObjectPascalMutableModuleExtensionImpl extends ObjectPascalModuleExtensionImpl implements ObjectPascalMutableModuleExtension {
    public ObjectPascalMutableModuleExtensionImpl(@Nonnull String id, @Nonnull ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @Nonnull
    @Override
    public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk() {
        return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
    }

    @Override
    public void setMainFilePath(@Nullable String path) {

    }

    @Override
    public void setOutputPath(@Nullable String outputPath) {

    }

    @RequiredUIAccess
    @Nullable
    @Override
    public Component createConfigurationComponent(@Nonnull Disposable disposable, @Nonnull Runnable runnable) {
        VerticalLayout root = VerticalLayout.create();
        root.add(ModuleExtensionBundleBoxBuilder.createAndDefine(this, disposable, runnable).build());
        return root;
    }

    @Override
    public void setEnabled(boolean b) {
        myIsEnabled = b;
    }

    @Override
    public boolean isModified(@Nonnull ObjectPascalModuleExtension objectPascalModuleExtension) {
        return isModifiedImpl(objectPascalModuleExtension);
    }
}
