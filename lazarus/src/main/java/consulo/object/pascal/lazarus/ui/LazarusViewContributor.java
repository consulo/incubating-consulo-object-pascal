package consulo.object.pascal.lazarus.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.view.ExternalProjectsView;
import consulo.externalSystem.view.ExternalSystemNode;
import consulo.externalSystem.view.ExternalSystemViewContributor;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.object.pascal.lazarus.externalSystem.LazarusPackageData;
import consulo.util.collection.MultiMap;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusViewContributor extends ExternalSystemViewContributor {
    @Override
    public ProjectSystemId getSystemId() {
        return LazarusConstants.SYSTEM_ID;
    }

    @Override
    public List<Key<?>> getKeys() {
        return List.of(LazarusPackageData.KEY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ExternalSystemNode<?>> createNodes(ExternalProjectsView view, MultiMap<Key<?>, DataNode<?>> dataNodes) {
        Collection<DataNode<?>> packageNodes = dataNodes.get(LazarusPackageData.KEY);
        if (packageNodes.isEmpty()) {
            return List.of();
        }
        List<ExternalSystemNode<?>> children = new ArrayList<>();
        for (DataNode<?> dataNode : packageNodes) {
            if (dataNode.getData() instanceof LazarusPackageData) {
                children.add(new LazarusPackageNode(view, (DataNode<LazarusPackageData>) dataNode));
            }
        }
        LazarusPackagesNode packagesNode = new LazarusPackagesNode(view);
        packagesNode.addAll(children);
        return List.of(packagesNode);
    }

    @Nullable
    @Override
    public String getDisplayName(DataNode<?> node) {
        return node.getData() instanceof LazarusPackageData data ? data.getName() : null;
    }
}
