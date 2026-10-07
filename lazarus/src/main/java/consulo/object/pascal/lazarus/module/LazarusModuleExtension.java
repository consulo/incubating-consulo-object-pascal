package consulo.object.pascal.lazarus.module;

import consulo.annotation.access.RequiredReadAction;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.object.pascal.module.extension.PascalBuildModuleExtension;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.Locale;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusModuleExtension extends ExternalSystemModuleExtensionImpl implements PascalBuildModuleExtension<ExternalSystemModuleExtensionImpl> {
    public static final String PROJECT_FILE_KEY = "lazarus.project.file";
    public static final String MAIN_FILE_KEY = "lazarus.main.file";
    public static final String BUILD_MODE_KEY = "lazarus.build.mode";
    public static final String TARGET_FILE_KEY = "lazarus.target.file";

    private static final String PACKAGE_EXTENSION = ".lpk";

    public LazarusModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer, LazarusConstants.SYSTEM_ID);
    }

    @Nullable
    public String getProjectFilePath() {
        return getOption(PROJECT_FILE_KEY);
    }

    @Nullable
    public String getBuildMode() {
        return getOption(BUILD_MODE_KEY);
    }

    @Nullable
    public String getTargetFilePath() {
        return getOption(TARGET_FILE_KEY);
    }

    @Nullable
    @Override
    public String getMainFilePath() {
        return getOption(MAIN_FILE_KEY);
    }

    @Nullable
    @Override
    public String getOutputPath() {
        return null;
    }

    public boolean isPackage() {
        String path = getProjectFilePath();
        return path != null && path.toLowerCase(Locale.ROOT).endsWith(PACKAGE_EXTENSION);
    }

    boolean hasSameOptions(LazarusModuleExtension other) {
        return myOptions.equals(other.myOptions);
    }

    @Nullable
    @RequiredReadAction
    @Override
    public Path getExecutable() {
        String target = getTargetFilePath();
        return target != null && !isPackage() ? Path.of(target) : null;
    }
}
