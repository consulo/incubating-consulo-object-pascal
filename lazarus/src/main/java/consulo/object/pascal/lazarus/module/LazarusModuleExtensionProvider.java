package consulo.object.pascal.lazarus.module;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusModuleExtensionProvider implements ModuleExtensionProvider<LazarusModuleExtension> {
    @Override
    public String getId() {
        return LazarusModuleExtension.ID;
    }

    @Override
    public LocalizeValue getName() {
        return LazarusLocalize.extensionName();
    }

    @Override
    public Image getIcon() {
        return ObjectPascalIconGroup.pascal_16x16();
    }

    @Override
    public ModuleExtension<LazarusModuleExtension> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new LazarusModuleExtension(getId(), moduleRootLayer);
    }

    @Override
    public MutableModuleExtension<LazarusModuleExtension> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new LazarusMutableModuleExtension(getId(), moduleRootLayer);
    }
}
