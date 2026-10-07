package consulo.object.pascal.lazarus.externalSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.ui.ExternalSystemIconProvider;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusIconProvider implements ExternalSystemIconProvider {
    @Override
    public ProjectSystemId getSystemId() {
        return LazarusConstants.SYSTEM_ID;
    }

    @Override
    public Image getProjectIcon() {
        return ObjectPascalIconGroup.pascal_16x16();
    }
}
