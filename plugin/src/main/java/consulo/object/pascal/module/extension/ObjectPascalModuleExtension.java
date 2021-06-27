package consulo.object.pascal.module.extension;

import consulo.module.extension.ModuleExtensionWithSdk;

/**
 * @author VISTALL
 * @since 24/05/2021
 */
public interface ObjectPascalModuleExtension extends ModuleExtensionWithSdk<ObjectPascalModuleExtension> {
    String getMainFilePath();

    String getOutputPath();
}
