package consulo.object.pascal.lazarus.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.ui.AbstractExternalSystemToolWindowFactory;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusToolWindowFactory extends AbstractExternalSystemToolWindowFactory {
    public LazarusToolWindowFactory() {
        super(LazarusConstants.SYSTEM_ID);
    }

    @Override
    public ToolWindowAnchor getAnchor() {
        return ToolWindowAnchor.RIGHT;
    }

    @Override
    public Image getIcon() {
        return ObjectPascalIconGroup.pascal_16x16();
    }
}
