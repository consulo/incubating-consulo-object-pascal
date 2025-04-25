package consulo.object.pascal.module.extension.impl;

import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.content.bundle.SdkType;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionWithSdkBase;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 27/06/2021
 */
public class ObjectPascalModuleExtensionImpl extends ModuleExtensionWithSdkBase<ObjectPascalModuleExtension> implements ObjectPascalModuleExtension {
    public ObjectPascalModuleExtensionImpl(@Nonnull String id, @Nonnull ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @Override
    public String getMainFilePath() {
        return null;
    }

    @Override
    public String getOutputPath() {
        return null;
    }

    @Nonnull
    @Override
    public Class<? extends SdkType> getSdkTypeClass() {
        return BasePascalSdkType.class;
    }
}
