package consulo.object.pascal.lazarus;

import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.annotation.access.RequiredReadAction;
import consulo.content.bundle.Sdk;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.lazarus.module.LazarusModuleExtension;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.process.cmd.GeneralCommandLine;
import consulo.virtualFileSystem.VirtualFile;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public final class LazarusBuild {
    private static final String FALLBACK_UNIT_OUTPUT = "lib/consulo";
    private static final String BUILD_ALL_FLAG = "-B";

    private LazarusBuild() {
    }

    @RequiredReadAction
    public static LazarusBuildCommand create(LazarusModuleExtension extension, boolean rebuild) {
        Module module = extension.getModule();
        String projectFile = extension.getProjectFilePath();
        if (extension.isPackage() || projectFile == null) {
            return LazarusBuildCommand.SKIP;
        }

        Path projectPath = Path.of(projectFile);
        LazarusBuildTool buildTool = LazarusBuildTool.find();
        if (buildTool != null) {
            GeneralCommandLine commandLine = buildTool.createBuildCommandLine(projectPath, extension.getBuildMode());
            if (rebuild) {
                commandLine.getParametersList().prependAll(BUILD_ALL_FLAG);
            }
            return LazarusBuildCommand.of(commandLine, projectPath.getParent());
        }
        return fpcBuild(module, extension, projectPath, rebuild);
    }

    @RequiredReadAction
    private static LazarusBuildCommand fpcBuild(Module module, LazarusModuleExtension extension, Path projectFile, boolean rebuild) {
        Sdk sdk = ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class);
        String mainFile = extension.getMainFilePath();
        String targetFile = extension.getTargetFilePath();
        String sdkHome = sdk != null ? sdk.getHomePath() : null;
        if (sdk == null || sdkHome == null || mainFile == null) {
            return LazarusBuildCommand.error(LazarusLocalize.buildLazbuildMissing(module.getName()));
        }

        Path projectDirectory = projectFile.getParent();
        GeneralCommandLine commandLine = new GeneralCommandLine(PascalSdkUtil.getFPCExecutable(sdkHome).getPath());
        commandLine.withWorkDirectory(projectDirectory.toString());
        commandLine.addParameter("-viewhnbq");
        if (rebuild) {
            commandLine.addParameter(BUILD_ALL_FLAG);
        }
        String debugOptions = BasePascalSdkType.getAdditionalData(sdk).getString(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG);
        if (debugOptions != null && !debugOptions.isBlank()) {
            for (String option : debugOptions.trim().split("\\s+")) {
                commandLine.addParameter(option);
            }
        }
        for (String directory : sourceDirectories(module)) {
            commandLine.addParameter("-Fu" + directory);
            commandLine.addParameter("-Fi" + directory);
        }
        Path unitOutput = projectDirectory.resolve(FALLBACK_UNIT_OUTPUT);
        unitOutput.toFile().mkdirs();
        commandLine.addParameter("-FU" + unitOutput);
        if (targetFile != null) {
            Path target = Path.of(targetFile);
            Path targetDirectory = target.getParent();
            if (targetDirectory != null) {
                targetDirectory.toFile().mkdirs();
            }
            commandLine.addParameter("-o" + target);
        }
        commandLine.addParameter(mainFile);
        return LazarusBuildCommand.of(commandLine, projectDirectory);
    }

    @RequiredReadAction
    private static Set<String> sourceDirectories(Module module) {
        Set<String> directories = new LinkedHashSet<>();
        ModuleRootManager.getInstance(module).orderEntries().recursively().withoutSdk().withoutLibraries().forEachModule(dependency -> {
            for (VirtualFile root : ModuleRootManager.getInstance(dependency).getContentFolderFiles(LanguageContentFolderScopes.production())) {
                directories.add(root.getPath());
            }
            return true;
        });
        return directories;
    }
}
