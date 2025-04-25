package consulo.object.pascal.module.extension.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-04-25
 */
@ExtensionImpl
public class ObjectPascalModuleExtensionProviderImpl implements ModuleExtensionProvider<ObjectPascalModuleExtension> {
    @Nonnull
    @Override
    public String getId() {
        return "pascal";
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("Pascal");
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return ObjectPascalIconGroup.pascal_16x16();
    }

    @Nonnull
    @Override
    public ModuleExtension<ObjectPascalModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
        return new ObjectPascalModuleExtensionImpl(getId(), moduleRootLayer);
    }

    @Nonnull
    @Override
    public MutableModuleExtension<ObjectPascalModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
        return new ObjectPascalMutableModuleExtensionImpl(getId(), moduleRootLayer);
    }
}
