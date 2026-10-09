package consulo.object.pascal.msbuild;

import consulo.platform.PlatformOperatingSystem;
import jakarta.annotation.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public record DelphiProjectModel(@Nullable Path mainSource,
                                 String config,
                                 String platform,
                                 List<Path> unitSearchPath,
                                 @Nullable Path dcuOutput,
                                 @Nullable Path exeOutput,
                                 @Nullable Path executable) {
    public static final String DELPHI_COMPILE_ITEM = "DelphiCompile";
    public static final String DCC_REFERENCE_ITEM = "DCCReference";
    public static final String DEFAULT_PLATFORM = "Win32";

    public static final String LIBRARY_APP_TYPE = "Library";

    private static final String DEFAULT_CONFIG = "Debug";

    public static DelphiProjectModel of(Path projectDir, Map<String, String> properties, PlatformOperatingSystem os) {
        Path mainSource = resolve(projectDir, properties.get("MainSource"));
        String config = valueOrDefault(properties.get("Config"), DEFAULT_CONFIG);
        String platform = valueOrDefault(properties.get("Platform"), DEFAULT_PLATFORM);
        Path dcuOutput = resolve(projectDir, properties.get("DCC_DcuOutput"));
        Path exeOutput = resolve(projectDir, properties.get("DCC_ExeOutput"));

        Set<Path> unitSearchPath = new LinkedHashSet<>();
        String searchPath = properties.get("DCC_UnitSearchPath");
        if (searchPath != null) {
            for (String entry : searchPath.split(";")) {
                Path dir = resolve(projectDir, entry);
                if (dir != null) {
                    unitSearchPath.add(dir);
                }
            }
        }

        Path executable = null;
        if (mainSource != null) {
            String name = mainSource.getFileName().toString();
            int dot = name.lastIndexOf('.');
            String baseName = dot > 0 ? name.substring(0, dot) : name;
            boolean library = LIBRARY_APP_TYPE.equalsIgnoreCase(valueOrDefault(properties.get("AppType"), ""));
            executable = (exeOutput != null ? exeOutput : projectDir).resolve(outputFileName(baseName, library, os));
        }

        return new DelphiProjectModel(mainSource, config, platform, new ArrayList<>(unitSearchPath), dcuOutput, exeOutput, executable);
    }

    private static String outputFileName(String baseName, boolean library, PlatformOperatingSystem os) {
        if (os.isWindows()) {
            return baseName + (library ? ".dll" : ".exe");
        }
        if (!library) {
            return baseName;
        }
        return "lib" + baseName + (os.isMac() ? ".dylib" : ".so");
    }

    private static String valueOrDefault(@Nullable String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    @Nullable
    private static Path resolve(Path projectDir, @Nullable String value) {
        if (value == null) {
            return null;
        }
        String path = value.trim().replace('\\', '/');
        if (path.isEmpty()) {
            return null;
        }
        try {
            Path candidate = Path.of(path);
            return (candidate.isAbsolute() ? candidate : projectDir.resolve(candidate)).normalize();
        }
        catch (InvalidPathException e) {
            return null;
        }
    }
}
