package consulo.object.pascal.lazarus;

import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record LazarusProjectFile(Path file,
                                 String title,
                                 @Nullable Path mainUnit,
                                 List<Path> units,
                                 List<String> buildModes,
                                 @Nullable String defaultBuildMode,
                                 LazarusSearchPaths paths,
                                 Map<String, LazarusSearchPaths> buildModePaths,
                                 List<LazarusPackageRef> requiredPackages) {
    public Path directory() {
        return file.getParent();
    }

    public LazarusSearchPaths pathsFor(@Nullable String buildMode) {
        if (buildMode != null) {
            LazarusSearchPaths modePaths = buildModePaths.get(buildMode);
            if (modePaths != null) {
                return modePaths;
            }
        }
        return paths;
    }
}
