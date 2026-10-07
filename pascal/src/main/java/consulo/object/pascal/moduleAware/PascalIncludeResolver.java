package consulo.object.pascal.moduleAware;

import consulo.content.base.SourcesOrderRootType;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
final class PascalIncludeResolver {
    private static final List<String> EXTENSIONS = List.of("", ".inc", ".pas", ".pp");

    private final ProjectFileIndex myFileIndex;
    private final Map<VirtualFile, Set<VirtualFile>> mySearchRoots = new HashMap<>();

    PascalIncludeResolver(ProjectFileIndex fileIndex) {
        myFileIndex = fileIndex;
    }

    @Nullable
    VirtualFile resolve(VirtualFile referencing, String name) {
        String path = name.replace('\\', '/');
        VirtualFile dir = referencing.getParent();
        VirtualFile found = dir != null ? find(dir, path) : null;
        if (found != null) {
            return found;
        }
        for (VirtualFile root : searchRoots(referencing)) {
            found = find(root, path);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Set<VirtualFile> searchRoots(VirtualFile referencing) {
        return mySearchRoots.computeIfAbsent(referencing, file -> {
            Set<VirtualFile> roots = new LinkedHashSet<>();
            Module module = myFileIndex.getModuleForFile(file);
            if (module != null) {
                roots.addAll(List.of(ModuleRootManager.getInstance(module).getSourceRoots()));
            }
            for (OrderEntry entry : myFileIndex.getOrderEntriesForFile(file)) {
                roots.addAll(List.of(entry.getFiles(SourcesOrderRootType.ID)));
            }
            return roots;
        });
    }

    @Nullable
    private static VirtualFile find(VirtualFile dir, String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        boolean hasExtension = fileName.indexOf('.') >= 0;
        for (String extension : EXTENSIONS) {
            if (hasExtension && !extension.isEmpty()) {
                break;
            }
            VirtualFile file = dir.findFileByRelativePath(path + extension);
            if (file != null && !file.isDirectory()) {
                return file;
            }
        }
        return null;
    }
}
