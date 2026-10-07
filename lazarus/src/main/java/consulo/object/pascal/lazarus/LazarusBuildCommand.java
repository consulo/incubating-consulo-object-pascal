package consulo.object.pascal.lazarus;

import consulo.localize.LocalizeValue;
import consulo.process.cmd.GeneralCommandLine;
import jakarta.annotation.Nullable;

import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public record LazarusBuildCommand(@Nullable GeneralCommandLine commandLine, @Nullable Path workingDirectory, LocalizeValue error) {
    public static final LazarusBuildCommand SKIP = new LazarusBuildCommand(null, null, LocalizeValue.empty());

    public static LazarusBuildCommand of(GeneralCommandLine commandLine, Path workingDirectory) {
        return new LazarusBuildCommand(commandLine, workingDirectory, LocalizeValue.empty());
    }

    public static LazarusBuildCommand error(LocalizeValue error) {
        return new LazarusBuildCommand(null, null, error);
    }
}
