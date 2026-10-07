package consulo.object.pascal.lazarus.ui;

import consulo.externalSystem.util.Order;
import consulo.externalSystem.view.ExternalProjectsView;
import consulo.externalSystem.view.ExternalSystemNode;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.tree.PresentationData;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@Order(ExternalSystemNode.BUILTIN_DEPENDENCIES_DATA_NODE_ORDER + 1)
public class LazarusPackagesNode extends ExternalSystemNode<Object> {
    public LazarusPackagesNode(ExternalProjectsView view) {
        super(view, null, null);
    }

    @Override
    public String getName() {
        return LazarusLocalize.viewPackagesNode().get();
    }

    @Override
    protected void update(PresentationData presentation) {
        super.update(presentation);
        presentation.setIcon(PlatformIconGroup.nodesPplibfolder());
    }
}
