package consulo.object.pascal.module.extension;

import consulo.annotation.access.RequiredReadAction;
import consulo.module.extension.ModuleExtension;
import jakarta.annotation.Nullable;

import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public interface PascalBuildModuleExtension<T extends ModuleExtension<T>> extends ModuleExtension<T> {
    @Nullable
    String getMainFilePath();

    @Nullable
    String getOutputPath();

    @Nullable
    @RequiredReadAction
    Path getExecutable();
}
