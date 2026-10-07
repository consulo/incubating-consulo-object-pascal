package consulo.object.pascal.lazarus.externalSystem;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusPackageData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<LazarusPackageData> KEY = Key.create(LazarusPackageData.class, ProjectKeys.LIBRARY_DEPENDENCY.getProcessingWeight() + 1);

    private final String myName;
    private final String myPackageFile;
    private final ArrayList<String> mySourceDirectories;
    private final ArrayList<String> myExcludedDirectories;

    public LazarusPackageData(String name, String packageFile, List<String> sourceDirectories, List<String> excludedDirectories) {
        super(LazarusConstants.SYSTEM_ID);
        myName = name;
        myPackageFile = packageFile;
        mySourceDirectories = new ArrayList<>(sourceDirectories);
        myExcludedDirectories = new ArrayList<>(excludedDirectories);
    }

    public String getName() {
        return myName;
    }

    public String getPackageFile() {
        return myPackageFile;
    }

    public List<String> getSourceDirectories() {
        return mySourceDirectories;
    }

    public List<String> getExcludedDirectories() {
        return myExcludedDirectories;
    }
}
