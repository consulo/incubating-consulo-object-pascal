package consulo.object.pascal.lazarus;

import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.PathEnvironmentVariableUtil;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import jakarta.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public final class LazarusBuildTool {
    private static final Logger LOG = Logger.getInstance(LazarusBuildTool.class);

    private static final String SEPARATOR = "|@|";
    private static final List<String> MACROS = List.of("LCLWidgetType", "TargetOS", "TargetCPU");
    private static final int PATH_VALUES = 5;
    private static final int FIXED_VALUES = PATH_VALUES + MACROS.size();
    private static final int EVALUATION_TIMEOUT = 60_000;
    private static final List<String> LAZARUS_DIRECTORIES = List.of(
        "/usr/lib/lazarus", "/usr/share/lazarus", "/usr/local/lib/lazarus", "/usr/local/share/lazarus", "/opt/lazarus",
        "C:\\lazarus", "C:\\Program Files\\Lazarus", "/Applications/Lazarus"
    );

    private final Path myLazbuild;
    @Nullable
    private final Path myLazarusDirectory;

    private LazarusBuildTool(Path lazbuild, @Nullable Path lazarusDirectory) {
        myLazbuild = lazbuild;
        myLazarusDirectory = lazarusDirectory;
    }

    @Nullable
    public static LazarusBuildTool find() {
        Path lazarusDirectory = findLazarusDirectory();
        Path lazbuild = findLazbuild(lazarusDirectory);
        return lazbuild != null ? new LazarusBuildTool(lazbuild, lazarusDirectory) : null;
    }

    public Path getLazbuild() {
        return myLazbuild;
    }

    public GeneralCommandLine createBuildCommandLine(Path projectFile, @Nullable String buildMode) {
        GeneralCommandLine commandLine = createCommandLine(projectFile, buildMode);
        commandLine.addParameter(projectFile.toString());
        return commandLine;
    }

    @Nullable
    public LazarusEvaluation evaluate(Path projectFile, @Nullable String buildMode, Collection<String> packageNames) {
        List<String> packages = new ArrayList<>(packageNames);
        StringBuilder template = new StringBuilder("$(TargetFile)" + SEPARATOR + "$(ProjOutDir)" + SEPARATOR + "$(ProjUnitPath)"
            + SEPARATOR + "$(ProjIncPath)" + SEPARATOR + "$(ProjSrcPath)");
        for (String macro : MACROS) {
            template.append(SEPARATOR).append("$(").append(macro).append(")");
        }
        for (String name : packages) {
            template.append(SEPARATOR).append("$PkgDir(").append(name).append(")");
        }

        GeneralCommandLine commandLine = createCommandLine(projectFile, buildMode);
        commandLine.addParameter("--get-expand-text=" + template);
        commandLine.addParameter(projectFile.toString());
        try {
            ProcessOutput output = CapturingProcessUtil.execAndGetOutput(commandLine, EVALUATION_TIMEOUT);
            List<String> lines = output.getStdoutLines();
            if (output.getExitCode() != 0 || output.isTimeout() || lines.isEmpty()) {
                LOG.warn("lazbuild could not evaluate " + projectFile + ": " + output.getStderr());
                return null;
            }
            String[] values = lines.get(lines.size() - 1).split(java.util.regex.Pattern.quote(SEPARATOR), -1);
            if (values.length != FIXED_VALUES + packages.size()) {
                LOG.warn("Unexpected lazbuild output for " + projectFile + ": " + lines.get(lines.size() - 1));
                return null;
            }
            Map<String, String> macros = new LinkedHashMap<>();
            for (int i = 0; i < MACROS.size(); i++) {
                String value = values[PATH_VALUES + i].trim();
                if (!value.isEmpty()) {
                    macros.put(MACROS.get(i), value);
                }
            }
            Map<String, Path> packageDirectories = new LinkedHashMap<>();
            for (int i = 0; i < packages.size(); i++) {
                Path directory = path(values[FIXED_VALUES + i]);
                if (directory != null) {
                    packageDirectories.put(packages.get(i), directory);
                }
            }
            return new LazarusEvaluation(path(values[0]), path(values[1]), paths(values[2]), paths(values[3]), paths(values[4]), macros, packageDirectories);
        }
        catch (ExecutionException e) {
            LOG.warn("lazbuild could not evaluate " + projectFile, e);
            return null;
        }
    }

    private GeneralCommandLine createCommandLine(Path projectFile, @Nullable String buildMode) {
        GeneralCommandLine commandLine = new GeneralCommandLine(myLazbuild.toString());
        commandLine.withWorkDirectory(projectFile.getParent().toString());
        if (myLazarusDirectory != null) {
            commandLine.addParameter("--lazarusdir=" + myLazarusDirectory);
        }
        if (buildMode != null) {
            commandLine.addParameter("--build-mode=" + buildMode);
        }
        return commandLine;
    }

    @Nullable
    private static Path findLazarusDirectory() {
        Path environmentOptions = Path.of(System.getProperty("user.home"), Platform.current().os().isWindows() ? "AppData/Local/lazarus" : ".lazarus",
            "environmentoptions.xml");
        if (Files.isRegularFile(environmentOptions)) {
            try {
                String directory = LazarusFileReader.readLazarusDirectory(environmentOptions);
                if (directory != null && isLazarusDirectory(Path.of(directory))) {
                    return Path.of(directory);
                }
            }
            catch (IOException e) {
                LOG.warn("Cannot read " + environmentOptions, e);
            }
        }
        for (String candidate : LAZARUS_DIRECTORIES) {
            Path directory = Path.of(candidate);
            if (isLazarusDirectory(directory)) {
                return directory;
            }
        }
        return null;
    }

    @Nullable
    private static Path findLazbuild(@Nullable Path lazarusDirectory) {
        String executable = Platform.current().os().isWindows() ? "lazbuild.exe" : "lazbuild";
        File inPath = PathEnvironmentVariableUtil.findInPath(executable);
        if (inPath != null) {
            return inPath.toPath();
        }
        if (lazarusDirectory != null) {
            Path candidate = lazarusDirectory.resolve(executable);
            if (Files.isExecutable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isLazarusDirectory(Path directory) {
        return Files.isDirectory(directory.resolve("lcl"));
    }

    @Nullable
    private static Path path(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() || trimmed.contains("$") ? null : Path.of(trimmed).normalize();
    }

    private static List<Path> paths(String value) {
        List<Path> result = new ArrayList<>();
        for (String part : value.split(";")) {
            Path path = path(part);
            if (path != null && !result.contains(path)) {
                result.add(path);
            }
        }
        return result;
    }
}
