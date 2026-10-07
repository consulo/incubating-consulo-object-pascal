package consulo.object.pascal.msbuild.module;

import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleExtension;
import consulo.object.pascal.msbuild.localize.DelphiLocalize;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class DelphiMutableModuleExtension extends DelphiModuleExtension implements MutableModuleExtension<DelphiModuleExtension> {
    public DelphiMutableModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    public void setMainFilePath(@Nullable String path) {
        myMainFilePath = path;
    }

    public void setOutputPath(@Nullable String outputPath) {
        myOutputPath = outputPath;
    }

    public void setProjectFilePath(@Nullable String path) {
        myProjectFilePath = path;
    }

    public void setConfiguration(@Nullable String configuration) {
        myConfiguration = configuration;
    }

    public void setPlatform(@Nullable String platform) {
        myPlatform = platform;
    }

    public void setExecutablePath(@Nullable String path) {
        myExecutablePath = path;
    }

    @Override
    public void setEnabled(boolean enabled) {
        myIsEnabled = enabled;
    }

    @Override
    public boolean isModified(DelphiModuleExtension originalExtension) {
        return isModifiedImpl(originalExtension);
    }

    @RequiredUIAccess
    @Nullable
    @Override
    public Component createConfigurationComponent(Disposable uiDisposable, Runnable updateOnCheck) {
        String configuration = myConfiguration != null ? myConfiguration : "";
        String platform = myPlatform != null ? myPlatform : "";
        return FormBuilder.create()
            .addLabeled(DelphiLocalize.extensionProjectFileLabel(), Label.create(LocalizeValue.of(myProjectFilePath != null ? myProjectFilePath : "")))
            .addLabeled(DelphiLocalize.extensionConfigurationLabel(), Label.create(LocalizeValue.of(configuration + "|" + platform)))
            .addLabeled(DelphiLocalize.extensionExecutableLabel(), Label.create(LocalizeValue.of(myExecutablePath != null ? myExecutablePath : "")))
            .build();
    }
}
