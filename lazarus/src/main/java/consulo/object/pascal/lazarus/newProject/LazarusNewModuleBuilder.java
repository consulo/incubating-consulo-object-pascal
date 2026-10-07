package consulo.object.pascal.lazarus.newProject;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.creation.scratch.NewModuleBuilder;
import consulo.module.creation.scratch.NewModuleContext;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.newProject.PascalNewModuleGroup;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class LazarusNewModuleBuilder implements NewModuleBuilder {
    @Override
    public void setupContext(NewModuleContext context) {
        PascalNewModuleGroup.get(context).add(LazarusLocalize.newProjectName(), ObjectPascalIconGroup.lazarus(), new LazarusNewModuleBuilderProcessor());
    }
}
