package consulo.object.pascal.lazarus.module.orderEntry;

import consulo.content.RootProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryModel;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusPackageOrderEntryModel implements CustomOrderEntryModel {
    private final String myName;
    private final String myPackageFileUrl;
    private final List<String> mySourceRootUrls;
    private final List<String> myExcludedRootUrls;
    private final LazarusPackageRootProvider myRootProvider = new LazarusPackageRootProvider(this);

    @Nullable
    private volatile Set<VirtualFile> mySourceRoots;
    @Nullable
    private volatile Set<VirtualFile> myExcludedRoots;

    public LazarusPackageOrderEntryModel(String name, String packageFileUrl, List<String> sourceRootUrls, List<String> excludedRootUrls) {
        myName = name;
        myPackageFileUrl = packageFileUrl;
        mySourceRootUrls = List.copyOf(sourceRootUrls);
        myExcludedRootUrls = List.copyOf(excludedRootUrls);
    }

    public String getName() {
        return myName;
    }

    public String getPackageFileUrl() {
        return myPackageFileUrl;
    }

    public List<String> getSourceRootUrls() {
        return mySourceRootUrls;
    }

    public List<String> getExcludedRootUrls() {
        return myExcludedRootUrls;
    }

    @Override
    public void bind(ModuleRootLayer moduleRootLayer) {
    }

    @Override
    public String getPresentableName() {
        return myName;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isSynthetic() {
        return true;
    }

    @Override
    public RootProvider getRootProvider() {
        return myRootProvider;
    }

    @Override
    public VirtualFile[] getExcludedRoots() {
        return VirtualFileUtil.toVirtualFileArray(excludedRoots());
    }

    @Override
    public Object getEqualObject() {
        return LazarusPackageOrderEntryType.ID + " " + myPackageFileUrl;
    }

    @Override
    public boolean isEquivalentTo(CustomOrderEntryModel model) {
        return model instanceof LazarusPackageOrderEntryModel other
            && myName.equals(other.myName)
            && myPackageFileUrl.equals(other.myPackageFileUrl)
            && mySourceRootUrls.equals(other.mySourceRootUrls)
            && myExcludedRootUrls.equals(other.myExcludedRootUrls);
    }

    @Override
    public LazarusPackageOrderEntryModel clone() {
        return new LazarusPackageOrderEntryModel(myName, myPackageFileUrl, mySourceRootUrls, myExcludedRootUrls);
    }

    Set<VirtualFile> sourceRoots() {
        Set<VirtualFile> roots = mySourceRoots;
        if (roots == null) {
            roots = resolve(mySourceRootUrls);
            mySourceRoots = roots;
        }
        return roots;
    }

    private Set<VirtualFile> excludedRoots() {
        Set<VirtualFile> roots = myExcludedRoots;
        if (roots == null) {
            roots = resolve(myExcludedRootUrls);
            myExcludedRoots = roots;
        }
        return roots;
    }

    private static Set<VirtualFile> resolve(List<String> urls) {
        if (urls.isEmpty()) {
            return Set.of();
        }
        VirtualFileManager fileManager = VirtualFileManager.getInstance();
        Set<VirtualFile> result = new LinkedHashSet<>();
        for (String url : urls) {
            VirtualFile file = fileManager.findFileByUrl(url);
            if (file != null) {
                result.add(file);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
