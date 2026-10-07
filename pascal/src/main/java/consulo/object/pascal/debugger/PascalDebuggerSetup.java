package consulo.object.pascal.debugger;

import consulo.container.plugin.PluginManager;
import consulo.nativeDev.debugger.driver.NativeDebuggerKind;
import consulo.nativeDev.debugger.driver.NativeDebuggerSetup;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class PascalDebuggerSetup implements NativeDebuggerSetup {
    public static final PascalDebuggerSetup INSTANCE = new PascalDebuggerSetup();

    private static final String PRINTERS_MODULE = "fpc_printers";

    @Override
    public List<String> getInitCommands(NativeDebuggerKind kind) {
        if (kind != NativeDebuggerKind.GDB) {
            return List.of();
        }
        Path printersDir = printersDir();
        if (printersDir == null) {
            return List.of();
        }
        String path = StringUtil.escapeStringCharacters(FileUtil.toSystemDependentName(printersDir.toString()));
        return List.of("python import sys; " +
            "sys.path.insert(0, \"" + path + "\"); " +
            "import " + PRINTERS_MODULE + "; " +
            PRINTERS_MODULE + ".register_printers(gdb)");
    }

    @Nullable
    private static Path printersDir() {
        File pluginPath = PluginManager.getPluginPath(PascalDebuggerSetup.class);
        if (pluginPath == null) {
            return null;
        }
        Path dir = pluginPath.toPath().resolve("debugger");
        return Files.isRegularFile(dir.resolve(PRINTERS_MODULE + ".py")) ? dir : null;
    }
}
