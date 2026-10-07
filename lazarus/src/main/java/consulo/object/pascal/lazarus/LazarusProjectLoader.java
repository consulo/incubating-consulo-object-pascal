package consulo.object.pascal.lazarus;

import consulo.logging.Logger;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public final class LazarusProjectLoader {
    private static final Logger LOG = Logger.getInstance(LazarusProjectLoader.class);

    private static final int MAX_PACKAGE_ROUNDS = 8;
    private static final int MAX_INSTALLED_PACKAGES = 200;

    private LazarusProjectLoader() {
    }

    public static LazarusProjectModel load(Path projectFile, @Nullable String buildMode, @Nullable LazarusBuildTool buildTool) throws IOException {
        LazarusProjectFile project = LazarusFileReader.readProject(projectFile);
        String mode = buildMode != null && project.buildModes().contains(buildMode) ? buildMode : null;

        Set<String> installedPackages = new LinkedHashSet<>();
        List<LazarusPackageFile> localPackages = readLocalPackages(project, installedPackages);

        LazarusEvaluation evaluation = null;
        List<LazarusInstalledPackage> installed = List.of();
        if (buildTool != null) {
            evaluation = buildTool.evaluate(projectFile, mode, installedPackages);
            if (evaluation != null) {
                installed = collectInstalledPackages(projectFile, mode, buildTool, evaluation, installedPackages, localPackages);
            }
        }
        return new LazarusProjectModel(project, mode, evaluation, localPackages, installed);
    }

    public static List<LazarusPackageFile> readLocalPackages(LazarusProjectFile project, Set<String> installedPackages) throws IOException {
        Map<String, LazarusPackageFile> localPackages = new LinkedHashMap<>();
        Deque<LazarusPackageRef> queue = new ArrayDeque<>(project.requiredPackages());
        while (!queue.isEmpty()) {
            LazarusPackageRef ref = queue.poll();
            String key = key(ref.name());
            if (localPackages.containsKey(key) || installedPackages.contains(ref.name())) {
                continue;
            }
            Path defaultFile = ref.defaultFile();
            if (defaultFile != null && Files.isRegularFile(defaultFile)) {
                LazarusPackageFile packageFile = LazarusFileReader.readPackage(defaultFile);
                localPackages.put(key, packageFile);
                queue.addAll(packageFile.requiredPackages());
            }
            else {
                installedPackages.add(ref.name());
            }
        }
        return new ArrayList<>(localPackages.values());
    }

    public static List<Path> sourceDirectories(LazarusPackageFile packageFile, Map<String, String> macros) {
        Set<Path> directories = new LinkedHashSet<>();
        Path directory = packageFile.directory();
        directories.add(directory);
        addSearchPaths(directories, directory, packageFile.paths().otherUnitFiles(), macros);
        addSearchPaths(directories, directory, packageFile.paths().includeFiles(), macros);
        return new ArrayList<>(directories);
    }

    private static void addSearchPaths(Set<Path> directories, Path directory, List<String> paths, Map<String, String> macros) {
        for (String path : paths) {
            String expanded = LazarusFileReader.expandMacros(path, macros);
            if (expanded != null) {
                addExisting(directories, directory.resolve(expanded).normalize());
            }
        }
    }

    private static List<LazarusInstalledPackage> collectInstalledPackages(Path projectFile,
                                                                         @Nullable String mode,
                                                                         LazarusBuildTool buildTool,
                                                                         LazarusEvaluation evaluation,
                                                                         Set<String> installedPackages,
                                                                         List<LazarusPackageFile> localPackages) {
        Map<String, Path> packageDirectories = new HashMap<>();
        evaluation.packageDirectories().forEach((name, path) -> packageDirectories.put(key(name), path));
        Set<String> visited = new HashSet<>();
        for (LazarusPackageFile packageFile : localPackages) {
            visited.add(key(packageFile.name()));
        }
        List<String> frontier = new ArrayList<>();
        for (String name : installedPackages) {
            if (visited.add(key(name))) {
                frontier.add(name);
            }
        }

        List<LazarusPackageFile> resolved = new ArrayList<>();
        for (int round = 0; round < MAX_PACKAGE_ROUNDS && !frontier.isEmpty() && visited.size() < MAX_INSTALLED_PACKAGES; round++) {
            List<String> next = new ArrayList<>();
            for (String name : frontier) {
                Path directory = packageDirectories.get(key(name));
                LazarusPackageFile packageFile = directory != null ? findPackageFile(directory, name) : null;
                if (packageFile == null) {
                    continue;
                }
                resolved.add(packageFile);
                for (LazarusPackageRef ref : packageFile.requiredPackages()) {
                    if (visited.add(key(ref.name()))) {
                        next.add(ref.name());
                    }
                }
            }
            if (next.isEmpty()) {
                break;
            }
            LazarusEvaluation more = buildTool.evaluate(projectFile, mode, next);
            if (more == null) {
                break;
            }
            more.packageDirectories().forEach((name, path) -> packageDirectories.put(key(name), path));
            frontier = next;
        }

        List<LazarusInstalledPackage> result = new ArrayList<>();
        for (LazarusPackageFile packageFile : resolved) {
            List<Path> sources = sourceDirectories(packageFile, evaluation.macros());
            result.add(new LazarusInstalledPackage(packageFile.name(), packageFile.file(), sources, excludedDirectories(sources)));
        }
        return result;
    }

    public static List<Path> excludedDirectories(List<Path> sourceDirectories) {
        Set<Path> sources = new HashSet<>(sourceDirectories);
        Set<Path> excluded = new LinkedHashSet<>();
        for (Path root : sourceDirectories) {
            collectExcluded(root, sources, excluded);
        }
        return new ArrayList<>(excluded);
    }

    private static void collectExcluded(Path directory, Set<Path> sources, Set<Path> excluded) {
        List<Path> children;
        try (Stream<Path> stream = Files.list(directory)) {
            children = stream.filter(Files::isDirectory).map(Path::normalize).toList();
        }
        catch (IOException e) {
            LOG.warn("Cannot list " + directory, e);
            return;
        }
        for (Path child : children) {
            if (sources.contains(child) || excluded.contains(child)) {
                continue;
            }
            if (sources.stream().anyMatch(source -> source.startsWith(child))) {
                collectExcluded(child, sources, excluded);
            }
            else {
                excluded.add(child);
            }
        }
    }

    @Nullable
    private static LazarusPackageFile findPackageFile(Path directory, String name) {
        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".lpk")).toList()) {
                LazarusPackageFile packageFile = LazarusFileReader.readPackage(file);
                if (packageFile.name().equalsIgnoreCase(name)) {
                    return packageFile;
                }
            }
        }
        catch (IOException e) {
            LOG.warn("Cannot read Lazarus package " + name + " in " + directory, e);
        }
        return null;
    }

    private static void addExisting(Set<Path> directories, @Nullable Path directory) {
        if (directory != null && Files.isDirectory(directory)) {
            directories.add(directory.normalize());
        }
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
