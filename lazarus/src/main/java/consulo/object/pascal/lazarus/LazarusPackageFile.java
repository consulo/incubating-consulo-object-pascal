package consulo.object.pascal.lazarus;

import java.nio.file.Path;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record LazarusPackageFile(Path file,
                                 String name,
                                 List<Path> files,
                                 LazarusSearchPaths paths,
                                 List<LazarusPackageRef> requiredPackages) {
    public Path directory() {
        return file.getParent();
    }
}
