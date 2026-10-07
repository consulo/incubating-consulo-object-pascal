package consulo.object.pascal.module.extension;

import consulo.module.extension.MutableModuleExtensionWithSdk;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27/06/2021
 */
public interface ObjectPascalMutableModuleExtension<T extends ObjectPascalModuleExtension<T>> extends ObjectPascalModuleExtension<T>, MutableModuleExtensionWithSdk<T> {
    void setMainFilePath(@Nullable String path);

    void setOutputPath(@Nullable String outputPath);
}
