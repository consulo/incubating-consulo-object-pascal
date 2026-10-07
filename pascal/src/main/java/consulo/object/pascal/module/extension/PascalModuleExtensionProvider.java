package consulo.object.pascal.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 2025-04-25
 */
@ExtensionImpl
public class PascalModuleExtensionProvider implements ModuleExtensionProvider<PascalModuleExtension> {
    @Override
    public String getId() {
        return PascalModuleExtension.ID;
    }

    @Override
    public LocalizeValue getName() {
        return ObjectPascalLocalize.extensionName();
    }

    @Override
    public Image getIcon() {
        return ObjectPascalIconGroup.pascal();
    }

    @Override
    public ModuleExtension<PascalModuleExtension> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new PascalModuleExtension(getId(), moduleRootLayer);
    }

    @Override
    public MutableModuleExtension<PascalModuleExtension> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new PascalMutableModuleExtension(getId(), moduleRootLayer);
    }
}
