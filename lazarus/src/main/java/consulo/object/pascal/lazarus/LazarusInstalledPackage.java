package consulo.object.pascal.lazarus;

import java.nio.file.Path;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record LazarusInstalledPackage(String name, Path file, List<Path> sourceDirectories, List<Path> excludedDirectories) {
}
