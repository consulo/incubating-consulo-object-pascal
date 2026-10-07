package consulo.object.pascal.lazarus.externalSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionProvider;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusExternalModuleExtensionProvider extends ExternalSystemModuleExtensionProvider {
    public LazarusExternalModuleExtensionProvider() {
        super(LazarusConstants.SYSTEM_ID);
    }
}
