package consulo.object.pascal.lazarus.module;

import consulo.disposer.Disposable;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtension;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusMutableModuleExtension extends LazarusModuleExtension implements ExternalSystemMutableModuleExtension<ExternalSystemModuleExtensionImpl> {
    public LazarusMutableModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    public void setProjectFilePath(@Nullable String path) {
        setOption(PROJECT_FILE_KEY, path);
    }

    public void setMainFilePath(@Nullable String path) {
        setOption(MAIN_FILE_KEY, path);
    }

    public void setBuildMode(@Nullable String buildMode) {
        setOption(BUILD_MODE_KEY, buildMode);
    }

    public void setTargetFilePath(@Nullable String path) {
        setOption(TARGET_FILE_KEY, path);
    }

    @Override
    public void setOption(String key, @Nullable String value) {
        if (value == null) {
            myOptions.remove(key);
        }
        else {
            myOptions.put(key, value);
        }
    }

    @Override
    public void removeOption(String key) {
        myOptions.remove(key);
    }

    @Override
    public void removeAllOptions() {
        myOptions.clear();
    }

    @Override
    public void setEnabled(boolean enabled) {
        myIsEnabled = enabled;
    }

    @Override
    public boolean isModified(ExternalSystemModuleExtensionImpl originalExtension) {
        return myIsEnabled != originalExtension.isEnabled()
            || !(originalExtension instanceof LazarusModuleExtension lazarus)
            || !hasSameOptions(lazarus);
    }

    @RequiredUIAccess
    @Nullable
    @Override
    public Component createConfigurationComponent(Disposable uiDisposable, Runnable updateOnCheck) {
        FormBuilder builder = FormBuilder.create();
        String projectFile = getProjectFilePath();
        builder.addLabeled(LazarusLocalize.extensionProjectFileLabel(), Label.create(LocalizeValue.of(projectFile != null ? projectFile : "")));
        if (!isPackage()) {
            String buildMode = getBuildMode();
            builder.addLabeled(LazarusLocalize.extensionBuildModeLabel(),
                Label.create(buildMode != null ? LocalizeValue.of(buildMode) : LazarusLocalize.extensionBuildModeDefault()));
        }
        return builder.build();
    }
}
