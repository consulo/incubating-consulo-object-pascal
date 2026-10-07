package consulo.object.pascal.lazarus.externalSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.object.pascal.lazarus.module.orderEntry.LazarusPackageEntries;
import consulo.object.pascal.lazarus.module.orderEntry.LazarusPackageOrderEntryModel;
import consulo.project.Project;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusPackageDataService implements ProjectDataService<LazarusPackageData, Module> {
    @Override
    public Key<LazarusPackageData> getTargetDataKey() {
        return LazarusPackageData.KEY;
    }

    @Override
    public void importData(Collection<DataNode<LazarusPackageData>> toImport, Project project, boolean synchronous) {
        Map<String, List<LazarusPackageOrderEntryModel>> byModule = new LinkedHashMap<>();
        for (DataNode<LazarusPackageData> node : toImport) {
            ModuleData moduleData = node.getData(ProjectKeys.MODULE);
            if (moduleData != null) {
                byModule.computeIfAbsent(moduleData.getInternalName(), name -> new ArrayList<>()).add(toModel(node.getData()));
            }
        }
        if (byModule.isEmpty()) {
            return;
        }
        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (Map.Entry<String, List<LazarusPackageOrderEntryModel>> entry : byModule.entrySet()) {
                    Module module = moduleManager.findModuleByName(entry.getKey());
                    if (module == null || module.isDisposed()) {
                        continue;
                    }
                    ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
                    LazarusPackageEntries.replace(rootModel, entry.getValue());
                    if (rootModel.isChanged()) {
                        rootModel.commit();
                    }
                    else {
                        rootModel.dispose();
                    }
                }
            }
        });
    }

    private static LazarusPackageOrderEntryModel toModel(LazarusPackageData data) {
        return new LazarusPackageOrderEntryModel(data.getName(), url(data.getPackageFile()), urls(data.getSourceDirectories()), urls(data.getExcludedDirectories()));
    }

    private static List<String> urls(List<String> paths) {
        List<String> urls = new ArrayList<>(paths.size());
        for (String path : paths) {
            urls.add(url(path));
        }
        return urls;
    }

    private static String url(String path) {
        return VirtualFileUtil.pathToUrl(path);
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}
