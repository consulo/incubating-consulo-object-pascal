package consulo.object.pascal.lazarus.externalSystem;

import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.logging.Logger;
import consulo.object.pascal.lazarus.LazarusFileReader;
import consulo.object.pascal.lazarus.LazarusPackageFile;
import consulo.object.pascal.lazarus.LazarusProjectFile;
import consulo.object.pascal.lazarus.LazarusProjectLoader;
import consulo.object.pascal.lazarus.setting.LazarusProjectSettings;
import consulo.object.pascal.lazarus.setting.LazarusSettings;
import consulo.project.Project;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusAutoImportAware implements ExternalSystemAutoImportAware {
    private static final Logger LOG = Logger.getInstance(LazarusAutoImportAware.class);

    @Nullable
    @Override
    public String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        String lowerCase = changedFileOrDirPath.toLowerCase(Locale.ROOT);
        if (!lowerCase.endsWith("." + LazarusConstants.PROJECT_EXTENSION) && !lowerCase.endsWith("." + LazarusConstants.PACKAGE_EXTENSION)) {
            return null;
        }
        Path changed;
        try {
            changed = Path.of(changedFileOrDirPath).toAbsolutePath().normalize();
        }
        catch (InvalidPathException e) {
            return null;
        }
        for (LazarusProjectSettings settings : LazarusSettings.getInstance(project).getLinkedProjectsSettings()) {
            String projectPath = settings.getExternalProjectPath();
            if (projectPath != null && getAffectedExternalProjectFilePaths(projectPath, project).contains(changed)) {
                return projectPath;
            }
        }
        return null;
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        Path projectFile = Path.of(projectPath).toAbsolutePath().normalize();
        List<Path> files = new ArrayList<>();
        files.add(projectFile);
        try {
            LazarusProjectFile projectData = LazarusFileReader.readProject(projectFile);
            for (LazarusPackageFile packageFile : LazarusProjectLoader.readLocalPackages(projectData, new HashSet<>())) {
                files.add(packageFile.file().toAbsolutePath().normalize());
            }
        }
        catch (IOException e) {
            LOG.warn("Cannot read Lazarus project " + projectPath, e);
        }
        return files;
    }
}
