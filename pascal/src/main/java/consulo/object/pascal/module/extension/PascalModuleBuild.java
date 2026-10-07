package consulo.object.pascal.module.extension;

import consulo.localize.LocalizeValue;
import consulo.process.cmd.GeneralCommandLine;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public record PascalModuleBuild(@Nullable GeneralCommandLine commandLine, LocalizeValue error) {
    public static final PascalModuleBuild SKIP = new PascalModuleBuild(null, LocalizeValue.empty());

    public static PascalModuleBuild of(GeneralCommandLine commandLine) {
        return new PascalModuleBuild(commandLine, LocalizeValue.empty());
    }

    public static PascalModuleBuild error(LocalizeValue error) {
        return new PascalModuleBuild(null, error);
    }
}
