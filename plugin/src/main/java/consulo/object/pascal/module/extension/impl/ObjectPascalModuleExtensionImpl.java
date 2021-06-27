package consulo.object.pascal.module.extension.impl;

import com.intellij.openapi.projectRoots.SdkTypeId;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.roots.ModuleRootLayer;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 27/06/2021
 */
public class ObjectPascalModuleExtensionImpl extends ModuleExtensionWithSdkImpl<ObjectPascalModuleExtension> implements ObjectPascalModuleExtension {
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
    public Class<? extends SdkTypeId> getSdkTypeClass() {
        return BasePascalSdkType.class;
    }
}
