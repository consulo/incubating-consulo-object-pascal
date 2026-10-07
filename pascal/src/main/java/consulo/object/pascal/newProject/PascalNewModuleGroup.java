package consulo.object.pascal.newProject;

import consulo.module.creation.scratch.NewModuleContext;
import consulo.module.creation.scratch.NewModuleContextGroup;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.localize.ObjectPascalLocalize;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public final class PascalNewModuleGroup {
    public static final String ID = "pascal";

    private PascalNewModuleGroup() {
    }

    public static NewModuleContextGroup get(NewModuleContext context) {
        return context.addGroup(ID, ObjectPascalLocalize.newProjectGroup(), ObjectPascalIconGroup.pascal());
    }
}
