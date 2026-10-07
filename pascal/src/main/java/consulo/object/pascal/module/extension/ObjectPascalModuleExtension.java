package consulo.object.pascal.module.extension;

import consulo.annotation.access.RequiredReadAction;
import consulo.module.extension.ModuleExtensionWithSdk;
import jakarta.annotation.Nullable;

import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 24/05/2021
 */
public interface ObjectPascalModuleExtension<T extends ObjectPascalModuleExtension<T>> extends ModuleExtensionWithSdk<T> {
    @Nullable
    String getMainFilePath();

    @Nullable
    String getOutputPath();

    @Nullable
    @RequiredReadAction
    default PascalModuleBuild createBuild() {
        return null;
    }

    @Nullable
    @RequiredReadAction
    default Path getExecutable() {
        return null;
    }
}
