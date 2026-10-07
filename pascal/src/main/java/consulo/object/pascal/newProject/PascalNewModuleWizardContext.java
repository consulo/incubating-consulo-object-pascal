package consulo.object.pascal.newProject;

import consulo.module.creation.NewModuleWizardContextBase;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class PascalNewModuleWizardContext extends NewModuleWizardContextBase {
    private PascalProjectKind myKind = PascalProjectKind.APPLICATION;

    public PascalNewModuleWizardContext(boolean isNewProject) {
        super(isNewProject);
    }

    public PascalProjectKind getKind() {
        return myKind;
    }

    public void setKind(PascalProjectKind kind) {
        myKind = kind;
    }
}
