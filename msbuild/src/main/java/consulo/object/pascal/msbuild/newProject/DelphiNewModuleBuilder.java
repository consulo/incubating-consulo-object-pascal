package consulo.object.pascal.msbuild.newProject;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.creation.scratch.NewModuleBuilder;
import consulo.module.creation.scratch.NewModuleContext;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.msbuild.localize.DelphiLocalize;
import consulo.object.pascal.newProject.PascalNewModuleGroup;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class DelphiNewModuleBuilder implements NewModuleBuilder {
    @Override
    public void setupContext(NewModuleContext context) {
        PascalNewModuleGroup.get(context).add(DelphiLocalize.newProjectName(), ObjectPascalIconGroup.delphi(), new DelphiNewModuleBuilderProcessor());
    }
}
