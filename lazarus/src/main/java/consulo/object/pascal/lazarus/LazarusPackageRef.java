package consulo.object.pascal.lazarus;

import jakarta.annotation.Nullable;

import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record LazarusPackageRef(String name, @Nullable Path defaultFile) {
}
