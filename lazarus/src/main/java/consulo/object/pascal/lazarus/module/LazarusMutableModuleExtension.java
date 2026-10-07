package consulo.object.pascal.lazarus.module;

import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.module.extension.ObjectPascalMutableModuleExtension;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;


/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusMutableModuleExtension extends LazarusModuleExtension implements ObjectPascalMutableModuleExtension<LazarusModuleExtension> {
    public LazarusMutableModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk() {
        return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
    }

    @Override
    public void setMainFilePath(@Nullable String path) {
        myMainFilePath = path;
    }

    @Override
    public void setOutputPath(@Nullable String outputPath) {
    }

    public void setProjectFilePath(@Nullable String path) {
        myProjectFilePath = path;
    }

    public void setBuildMode(@Nullable String buildMode) {
        myBuildMode = buildMode;
    }

    public void setTargetFilePath(@Nullable String path) {
        myTargetFilePath = path;
    }

    @Override
    public void setEnabled(boolean enabled) {
        myIsEnabled = enabled;
    }

    @Override
    public boolean isModified(LazarusModuleExtension originalExtension) {
        return isModifiedImpl(originalExtension);
    }

    @RequiredUIAccess
    @Nullable
    @Override
    public Component createConfigurationComponent(Disposable uiDisposable, Runnable updateOnCheck) {
        VerticalLayout root = VerticalLayout.create();
        root.add(ModuleExtensionBundleBoxBuilder.createAndDefine(this, uiDisposable, updateOnCheck).build());

        FormBuilder builder = FormBuilder.create();
        String projectFile = myProjectFilePath;
        builder.addLabeled(LazarusLocalize.extensionProjectFileLabel(), Label.create(LocalizeValue.of(projectFile != null ? projectFile : "")));
        if (!isPackage()) {
            String buildMode = myBuildMode;
            builder.addLabeled(LazarusLocalize.extensionBuildModeLabel(),
                Label.create(buildMode != null ? LocalizeValue.of(buildMode) : LazarusLocalize.extensionBuildModeDefault()));
        }
        root.add(builder.build());
        return root;
    }
}
