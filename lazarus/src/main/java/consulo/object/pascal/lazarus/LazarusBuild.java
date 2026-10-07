package consulo.object.pascal.lazarus;

import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.annotation.access.RequiredReadAction;
import consulo.content.bundle.Sdk;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.lazarus.module.LazarusModuleExtension;
import consulo.object.pascal.module.extension.PascalModuleBuild;
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

    private LazarusBuild() {
    }

    @RequiredReadAction
    public static PascalModuleBuild create(LazarusModuleExtension extension) {
        Module module = extension.getModule();
        String projectFile = extension.getProjectFilePath();
        if (extension.isPackage() || projectFile == null) {
            return PascalModuleBuild.SKIP;
        }

        LazarusBuildTool buildTool = LazarusBuildTool.find();
        if (buildTool != null) {
            return PascalModuleBuild.of(buildTool.createBuildCommandLine(Path.of(projectFile), extension.getBuildMode()));
        }
        return fpcBuild(module, extension, Path.of(projectFile));
    }

    @RequiredReadAction
    private static PascalModuleBuild fpcBuild(Module module, LazarusModuleExtension extension, Path projectFile) {
        Sdk sdk = extension.getSdk();
        String mainFile = extension.getMainFilePath();
        String targetFile = extension.getTargetFilePath();
        String sdkHome = sdk != null ? sdk.getHomePath() : null;
        if (sdk == null || sdkHome == null || mainFile == null) {
            return PascalModuleBuild.error(LazarusLocalize.buildLazbuildMissing(module.getName()));
        }

        Path projectDirectory = projectFile.getParent();
        GeneralCommandLine commandLine = new GeneralCommandLine(PascalSdkUtil.getFPCExecutable(sdkHome).getPath());
        commandLine.withWorkDirectory(projectDirectory.toString());
        commandLine.addParameter("-viewhnbq");
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
        return PascalModuleBuild.of(commandLine);
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
