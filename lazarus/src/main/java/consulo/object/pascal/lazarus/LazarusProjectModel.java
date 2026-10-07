package consulo.object.pascal.lazarus;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record LazarusProjectModel(LazarusProjectFile project,
                                  @Nullable String buildMode,
                                  @Nullable LazarusEvaluation evaluation,
                                  List<LazarusPackageFile> localPackages,
                                  List<LazarusInstalledPackage> installedPackages) {
}
