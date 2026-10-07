package consulo.object.pascal.lazarus.module;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.object.pascal.module.extension.PascalModuleExtension;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusModuleExtensionProvider extends ExternalSystemModuleExtensionProvider {
    public LazarusModuleExtensionProvider() {
        super(LazarusConstants.SYSTEM_ID);
    }

    @Nullable
    @Override
    public String getParentId() {
        return PascalModuleExtension.ID;
    }

    @Override
    public ModuleExtension<ExternalSystemModuleExtensionImpl> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new LazarusModuleExtension(getId(), moduleRootLayer);
    }

    @Override
    public MutableModuleExtension<ExternalSystemModuleExtensionImpl> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new LazarusMutableModuleExtension(getId(), moduleRootLayer);
    }
}
