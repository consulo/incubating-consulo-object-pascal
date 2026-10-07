package consulo.object.pascal.lazarus;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record LazarusSearchPaths(List<String> otherUnitFiles,
                                 List<String> includeFiles,
                                 @Nullable String unitOutputDirectory,
                                 @Nullable String targetFilename) {
    public static final LazarusSearchPaths EMPTY = new LazarusSearchPaths(List.of(), List.of(), null, null);
}
