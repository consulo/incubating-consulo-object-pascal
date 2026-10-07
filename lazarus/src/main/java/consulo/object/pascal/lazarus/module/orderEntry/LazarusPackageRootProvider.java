package consulo.object.pascal.lazarus.module.orderEntry;

import consulo.content.RootProviderBase;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
class LazarusPackageRootProvider extends RootProviderBase {
    private final LazarusPackageOrderEntryModel myModel;

    LazarusPackageRootProvider(LazarusPackageOrderEntryModel model) {
        myModel = model;
    }

    @Override
    public String[] getUrls(String rootType) {
        Set<VirtualFile> roots = roots(rootType);
        String[] urls = new String[roots.size()];
        int i = 0;
        for (VirtualFile root : roots) {
            urls[i++] = root.getUrl();
        }
        return urls;
    }

    @Override
    public VirtualFile[] getFiles(String rootType) {
        return VirtualFileUtil.toVirtualFileArray(roots(rootType));
    }

    private Set<VirtualFile> roots(String rootType) {
        return SourcesOrderRootType.ID.equals(rootType) || BinariesOrderRootType.ID.equals(rootType) ? myModel.sourceRoots() : Set.of();
    }
}
