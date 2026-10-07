package consulo.object.pascal.module.extension;

import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.content.bundle.SdkType;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionWithSdkBase;

/**
 * @author VISTALL
 * @since 27/06/2021
 */
public class PascalModuleExtension extends ModuleExtensionWithSdkBase<PascalModuleExtension> implements ObjectPascalModuleExtension<PascalModuleExtension> {
    public static final String ID = "pascal";

    public PascalModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @Override
    public Class<? extends SdkType> getSdkTypeClass() {
        return BasePascalSdkType.class;
    }
}
