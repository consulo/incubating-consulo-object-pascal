package consulo.object.pascal.lazarus.externalSystem;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.project.ModuleDependencyData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.object.pascal.lazarus.LazarusBuildTool;
import consulo.object.pascal.lazarus.LazarusEvaluation;
import consulo.object.pascal.lazarus.LazarusFileReader;
import consulo.object.pascal.lazarus.LazarusInstalledPackage;
import consulo.object.pascal.lazarus.LazarusPackageFile;
import consulo.object.pascal.lazarus.LazarusPackageRef;
import consulo.object.pascal.lazarus.LazarusProjectFile;
import consulo.object.pascal.lazarus.LazarusProjectLoader;
import consulo.object.pascal.lazarus.LazarusProjectModel;
import consulo.object.pascal.lazarus.LazarusSearchPaths;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.lazarus.setting.LazarusExecutionSettings;
import consulo.platform.Platform;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusProjectResolver implements ExternalSystemProjectResolver<LazarusExecutionSettings> {
    private static final String BACKUP_DIRECTORY = "backup";
    private static final String PACKAGE_MODULE_SUFFIX = " (package)";

    @Nullable
    @Override
    public DataNode<ProjectData> resolveProjectInfo(ExternalSystemTaskId id,
                                                    String projectPath,
                                                    boolean isPreviewMode,
                                                    @Nullable LazarusExecutionSettings settings,
                                                    ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        Path projectFile = Path.of(projectPath).toAbsolutePath().normalize();
        String buildMode = settings != null ? settings.getBuildMode() : null;
        String sdkName = settings != null ? settings.getSdkName() : null;

        LazarusBuildTool buildTool = isPreviewMode ? null : LazarusBuildTool.find();
        listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, LazarusLocalize.resolveStatus(projectFile.getFileName().toString()).get()));

        LazarusProjectModel model;
        try {
            model = LazarusProjectLoader.load(projectFile, buildMode, buildTool);
        }
        catch (IOException e) {
            throw new ExternalSystemException(LazarusLocalize.reloadFailed(projectPath, e.getMessage()).get());
        }
        return createProjectNode(projectPath, model, sdkName);
    }

    private static DataNode<ProjectData> createProjectNode(String projectPath, LazarusProjectModel model, @Nullable String sdkName) {
        LazarusProjectFile project = model.project();
        LazarusEvaluation evaluation = model.evaluation();
        Path projectDirectory = project.directory();
        String projectName = projectName(project);

        ProjectData projectData = new ProjectData(LazarusConstants.SYSTEM_ID, projectName, projectDirectory.toString(), projectPath);
        DataNode<ProjectData> projectNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        ModuleData projectModule = new ModuleData(projectName, LazarusConstants.SYSTEM_ID, projectName, projectDirectory.toString(), projectPath);
        DataNode<ModuleData> projectModuleNode = projectNode.createChild(ProjectKeys.MODULE, projectModule);

        Map<String, ModuleData> packageModules = new LinkedHashMap<>();
        Map<String, DataNode<ModuleData>> packageModuleNodes = new LinkedHashMap<>();
        Set<Path> packageDirectories = new LinkedHashSet<>();
        for (LazarusPackageFile packageFile : model.localPackages()) {
            String name = packageFile.name().equalsIgnoreCase(projectName) ? packageFile.name() + PACKAGE_MODULE_SUFFIX : packageFile.name();
            ModuleData moduleData = new ModuleData(name,
                LazarusConstants.SYSTEM_ID,
                name,
                packageFile.directory().toString(),
                packageFile.file().toString());
            packageModules.put(key(packageFile.name()), moduleData);
            packageModuleNodes.put(key(packageFile.name()), projectNode.createChild(ProjectKeys.MODULE, moduleData));
            packageDirectories.add(packageFile.directory());
        }

        LazarusSearchPaths paths = project.pathsFor(model.buildMode());
        Path targetFile = evaluation != null && evaluation.targetFile() != null ? evaluation.targetFile() : fallbackTarget(project, paths);
        Path unitOutput = evaluation != null && evaluation.unitOutputDirectory() != null
            ? evaluation.unitOutputDirectory()
            : expandedPath(projectDirectory, paths.unitOutputDirectory(), macros(evaluation));

        addProjectContent(projectModuleNode, project, evaluation, paths, packageDirectories, targetFile, unitOutput);
        setCompileOutput(projectModule, projectDirectory, unitOutput);

        Path mainUnit = project.mainUnit();
        projectModuleNode.createChild(LazarusModuleData.KEY, new LazarusModuleData(project.file().toString(),
            mainUnit != null ? mainUnit.toString() : null,
            model.buildMode(),
            targetFile.toString(),
            sdkName,
            null));

        addModuleDependencies(projectModuleNode, projectModule, project.requiredPackages(), packageModules);
        addPackages(projectModuleNode, model.installedPackages());
        addTasks(projectModuleNode, projectPath, project);

        for (LazarusPackageFile packageFile : model.localPackages()) {
            DataNode<ModuleData> moduleNode = packageModuleNodes.get(key(packageFile.name()));
            ModuleData moduleData = moduleNode.getData();
            Path directory = packageFile.directory();
            Map<String, String> macros = macros(evaluation);

            Set<Path> sources = new LinkedHashSet<>(LazarusProjectLoader.sourceDirectories(packageFile, macros));
            sources.removeIf(path -> !path.startsWith(directory));
            Path packageOutput = expandedPath(directory, packageFile.paths().unitOutputDirectory(), macros);
            Set<Path> excluded = new LinkedHashSet<>();
            addExcluded(excluded, packageOutput, directory, sources);
            addExcluded(excluded, directory.resolve(BACKUP_DIRECTORY), directory, sources);
            addContentRoot(moduleNode, directory, sources, excluded);
            setCompileOutput(moduleData, directory, packageOutput);

            moduleNode.createChild(LazarusModuleData.KEY, new LazarusModuleData(packageFile.file().toString(),
                null,
                null,
                null,
                null,
                projectModule.getInternalName()));

            addModuleDependencies(moduleNode, moduleData, packageFile.requiredPackages(), packageModules);
            addPackages(moduleNode, model.installedPackages());
        }
        return projectNode;
    }

    private static void addProjectContent(DataNode<ModuleData> moduleNode,
                                          LazarusProjectFile project,
                                          @Nullable LazarusEvaluation evaluation,
                                          LazarusSearchPaths paths,
                                          Set<Path> packageDirectories,
                                          Path targetFile,
                                          @Nullable Path unitOutput) {
        Path projectDirectory = project.directory();
        Set<Path> sources = new LinkedHashSet<>();
        for (Path unit : project.units()) {
            addSource(sources, unit.getParent(), projectDirectory, packageDirectories);
        }
        if (evaluation != null) {
            for (Path path : concat(evaluation.unitPaths(), evaluation.includePaths(), evaluation.sourcePaths())) {
                addSource(sources, path, projectDirectory, packageDirectories);
            }
        }
        List<Path> externalSources = new ArrayList<>();
        List<String> searchPaths = new ArrayList<>(paths.otherUnitFiles());
        searchPaths.addAll(paths.includeFiles());
        for (String path : searchPaths) {
            String expanded = LazarusFileReader.expandMacros(path, macros(evaluation));
            if (expanded == null) {
                continue;
            }
            Path resolved = projectDirectory.resolve(expanded).normalize();
            if (resolved.startsWith(projectDirectory)) {
                addSource(sources, resolved, projectDirectory, packageDirectories);
            }
            else if (Files.isDirectory(resolved) && packageDirectories.stream().noneMatch(resolved::startsWith) && !externalSources.contains(resolved)) {
                externalSources.add(resolved);
            }
        }

        Set<Path> excluded = new LinkedHashSet<>();
        addExcluded(excluded, unitOutput, projectDirectory, sources);
        addExcluded(excluded, targetFile.getParent(), projectDirectory, sources);
        addExcluded(excluded, projectDirectory.resolve(BACKUP_DIRECTORY), projectDirectory, sources);
        addContentRoot(moduleNode, projectDirectory, sources, excluded);

        for (Path external : externalSources) {
            addContentRoot(moduleNode, external, Set.of(external), Set.of());
        }
    }

    private static void addContentRoot(DataNode<ModuleData> moduleNode, Path root, Set<Path> sources, Set<Path> excluded) {
        ContentRootData contentRoot = new ContentRootData(LazarusConstants.SYSTEM_ID, root.toString());
        for (Path source : sources) {
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, source.toString());
        }
        for (Path folder : excluded) {
            contentRoot.storePath(ExternalSystemSourceType.EXCLUDED, folder.toString());
        }
        moduleNode.createChild(ProjectKeys.CONTENT_ROOT, contentRoot);
    }

    private static void setCompileOutput(ModuleData moduleData, Path root, @Nullable Path output) {
        if (output == null || !output.startsWith(root) || output.equals(root)) {
            return;
        }
        moduleData.setInheritProjectCompileOutputPath(false);
        moduleData.setCompileOutputPath(ExternalSystemSourceType.SOURCE, output.toString());
    }

    private static void addModuleDependencies(DataNode<ModuleData> moduleNode,
                                              ModuleData owner,
                                              List<LazarusPackageRef> requiredPackages,
                                              Map<String, ModuleData> packageModules) {
        Set<ModuleData> added = new LinkedHashSet<>();
        for (LazarusPackageRef ref : requiredPackages) {
            ModuleData target = packageModules.get(key(ref.name()));
            if (target != null && target != owner && added.add(target)) {
                moduleNode.createChild(ProjectKeys.MODULE_DEPENDENCY, new ModuleDependencyData(owner, target));
            }
        }
    }

    private static void addPackages(DataNode<ModuleData> moduleNode, List<LazarusInstalledPackage> installedPackages) {
        for (LazarusInstalledPackage installedPackage : installedPackages) {
            if (installedPackage.sourceDirectories().isEmpty()) {
                continue;
            }
            moduleNode.createChild(LazarusPackageData.KEY, new LazarusPackageData(installedPackage.name(),
                installedPackage.file().toString(),
                paths(installedPackage.sourceDirectories()),
                paths(installedPackage.excludedDirectories())));
        }
    }

    private static List<String> paths(List<Path> paths) {
        List<String> result = new ArrayList<>(paths.size());
        for (Path path : paths) {
            result.add(path.toString());
        }
        return result;
    }

    private static void addTasks(DataNode<ModuleData> moduleNode, String projectPath, LazarusProjectFile project) {
        String buildGroup = LazarusLocalize.taskGroupBuild().get();
        TaskData build = new TaskData(LazarusConstants.SYSTEM_ID, LazarusTaskManager.BUILD_TASK, projectPath, LazarusLocalize.taskBuildDescription().get());
        build.setGroup(buildGroup);
        moduleNode.createChild(ProjectKeys.TASK, build);

        TaskData rebuild = new TaskData(LazarusConstants.SYSTEM_ID, LazarusTaskManager.REBUILD_TASK, projectPath, LazarusLocalize.taskRebuildDescription().get());
        rebuild.setGroup(buildGroup);
        moduleNode.createChild(ProjectKeys.TASK, rebuild);

        if (project.buildModes().size() > 1) {
            String modesGroup = LazarusLocalize.taskGroupBuildModes().get();
            for (String buildMode : project.buildModes()) {
                TaskData task = new TaskData(LazarusConstants.SYSTEM_ID,
                    LazarusTaskManager.BUILD_MODE_TASK_PREFIX + buildMode,
                    projectPath,
                    LazarusLocalize.taskBuildModeDescription(buildMode).get());
                task.setGroup(modesGroup);
                moduleNode.createChild(ProjectKeys.TASK, task);
            }
        }
    }

    private static String projectName(LazarusProjectFile project) {
        String fileName = project.file().getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String name = dot > 0 ? fileName.substring(0, dot) : fileName;
        return name.replace('/', '_').replace('\\', '_');
    }

    private static Map<String, String> macros(@Nullable LazarusEvaluation evaluation) {
        return evaluation != null ? evaluation.macros() : Map.of();
    }

    private static void addSource(Set<Path> sources, @Nullable Path directory, Path root, Set<Path> packageDirectories) {
        if (directory == null) {
            return;
        }
        Path normalized = directory.normalize();
        if (normalized.startsWith(root) && Files.isDirectory(normalized) && packageDirectories.stream().noneMatch(normalized::startsWith)) {
            sources.add(normalized);
        }
    }

    private static void addExcluded(Set<Path> excluded, @Nullable Path directory, Path root, Set<Path> sources) {
        if (directory == null) {
            return;
        }
        Path normalized = directory.normalize();
        if (!normalized.startsWith(root) || normalized.equals(root)) {
            return;
        }
        Path top = root.resolve(root.relativize(normalized).getName(0));
        if (sources.stream().noneMatch(source -> source.startsWith(top))) {
            excluded.add(top);
        }
    }

    @Nullable
    private static Path expandedPath(Path root, @Nullable String path, Map<String, String> macros) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String expanded = LazarusFileReader.expandMacros(path, macros);
        if (expanded != null) {
            return root.resolve(expanded).normalize();
        }
        int macro = path.indexOf('$');
        int slash = path.lastIndexOf('/', macro);
        String prefix = slash < 0 ? "" : path.substring(0, slash);
        return prefix.isBlank() ? null : root.resolve(prefix).normalize();
    }

    private static Path fallbackTarget(LazarusProjectFile project, LazarusSearchPaths paths) {
        String target = paths.targetFilename();
        if (target == null && project.paths() != paths) {
            target = project.paths().targetFilename();
        }
        if (target == null || !LazarusFileReader.isMacroFree(target)) {
            target = projectName(project);
        }
        Path file = project.directory().resolve(target).normalize();
        if (Platform.current().os().isWindows() && !file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".exe")) {
            file = file.resolveSibling(file.getFileName() + ".exe");
        }
        return file;
    }

    @SafeVarargs
    private static List<Path> concat(List<Path>... lists) {
        List<Path> result = new ArrayList<>();
        for (List<Path> list : lists) {
            result.addAll(list);
        }
        return result;
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId taskId, ExternalSystemTaskNotificationListener listener) {
        return false;
    }
}
