package consulo.object.pascal.lazarus;

import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record LazarusEvaluation(@Nullable Path targetFile,
                                @Nullable Path unitOutputDirectory,
                                List<Path> unitPaths,
                                List<Path> includePaths,
                                List<Path> sourcePaths,
                                Map<String, String> macros,
                                Map<String, Path> packageDirectories) {
}
