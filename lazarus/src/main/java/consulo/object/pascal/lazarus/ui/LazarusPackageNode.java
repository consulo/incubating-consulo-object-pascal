package consulo.object.pascal.lazarus.ui;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.view.ExternalProjectsView;
import consulo.externalSystem.view.ExternalSystemNode;
import consulo.object.pascal.lazarus.externalSystem.LazarusPackageData;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.tree.PresentationData;

import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusPackageNode extends ExternalSystemNode<LazarusPackageData> {
    public LazarusPackageNode(ExternalProjectsView view, DataNode<LazarusPackageData> dataNode) {
        super(view, null, dataNode);
    }

    @Override
    protected void update(PresentationData presentation) {
        LazarusPackageData data = getData();
        Path packageFile = data != null ? Path.of(data.getPackageFile()) : null;
        Path directory = packageFile != null ? packageFile.getParent() : null;
        setNameAndTooltip(presentation, getName(), packageFile != null ? packageFile.toString() : null, directory != null ? directory.toString() : null);
        presentation.setIcon(PlatformIconGroup.nodesPplib());
    }
}
