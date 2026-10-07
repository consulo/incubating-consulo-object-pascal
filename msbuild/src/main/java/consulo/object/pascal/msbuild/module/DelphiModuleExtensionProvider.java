package consulo.object.pascal.msbuild.module;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.module.extension.PascalModuleExtension;
import consulo.object.pascal.msbuild.localize.DelphiLocalize;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class DelphiModuleExtensionProvider implements ModuleExtensionProvider<DelphiModuleExtension> {
    @Override
    public String getId() {
        return DelphiModuleExtension.ID;
    }

    @Nullable
    @Override
    public String getParentId() {
        return PascalModuleExtension.ID;
    }

    @Override
    public boolean isSystemOnly() {
        return true;
    }

    @Override
    public LocalizeValue getName() {
        return DelphiLocalize.extensionName();
    }

    @Override
    public Image getIcon() {
        return ObjectPascalIconGroup.delphi();
    }

    @Override
    public ModuleExtension<DelphiModuleExtension> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new DelphiModuleExtension(getId(), moduleRootLayer);
    }

    @Override
    public MutableModuleExtension<DelphiModuleExtension> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new DelphiMutableModuleExtension(getId(), moduleRootLayer);
    }
}
