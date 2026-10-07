package consulo.object.pascal.lazarus.externalSystem;

import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
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
import consulo.object.pascal.lazarus.module.LazarusMutableModuleExtension;
import consulo.object.pascal.lazarus.module.orderEntry.LazarusPackageEntries;
import consulo.object.pascal.module.extension.PascalMutableModuleExtension;
import consulo.object.pascal.sdk.PascalModuleSdkUtil;
import consulo.project.Project;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusModuleDataService implements ProjectDataService<LazarusModuleData, Module> {
    @Override
    public Key<LazarusModuleData> getTargetDataKey() {
        return LazarusModuleData.KEY;
    }

    @Override
    public void importData(Collection<DataNode<LazarusModuleData>> toImport, Project project, boolean synchronous) {
        if (toImport.isEmpty()) {
            return;
        }
        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (DataNode<LazarusModuleData> node : toImport) {
                    ModuleData moduleData = node.getData(ProjectKeys.MODULE);
                    Module module = moduleData != null ? moduleManager.findModuleByName(moduleData.getInternalName()) : null;
                    if (module != null && !module.isDisposed()) {
                        DataNode<?> moduleNode = node.getParent();
                        boolean hasPackages = moduleNode != null && ExternalSystemApiUtil.find(moduleNode, LazarusPackageData.KEY) != null;
                        apply(module, node.getData(), hasPackages);
                    }
                }
            }
        });
    }

    private static void apply(Module module, LazarusModuleData data, boolean hasPackages) {
        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        LazarusMutableModuleExtension extension = rootModel.getExtensionWithoutCheck(LazarusMutableModuleExtension.class);
        PascalMutableModuleExtension pascalExtension = rootModel.getExtensionWithoutCheck(PascalMutableModuleExtension.class);
        if (extension == null || pascalExtension == null) {
            rootModel.dispose();
            return;
        }
        extension.setEnabled(true);
        extension.setProjectFilePath(data.getProjectFile());
        extension.setMainFilePath(data.getMainFile());
        extension.setBuildMode(data.getBuildMode());
        extension.setTargetFilePath(data.getTargetFile());

        pascalExtension.setEnabled(true);
        String sdkOwner = data.getSdkOwnerModuleName();
        if (sdkOwner != null) {
            pascalExtension.getInheritableSdk().set(sdkOwner, null);
        }
        else if (data.getSdkName() != null) {
            pascalExtension.getInheritableSdk().set(null, data.getSdkName());
        }
        else if (pascalExtension.getInheritableSdk().isNull()) {
            Sdk sdk = PascalModuleSdkUtil.findDefaultSdk(FPCSdkType.getInstance());
            if (sdk != null) {
                pascalExtension.getInheritableSdk().set(null, sdk);
            }
        }
        PascalModuleSdkUtil.ensureSdkEntry(rootModel, pascalExtension);
        if (!hasPackages) {
            LazarusPackageEntries.removeAll(rootModel);
        }
        rootModel.commit();
    }


    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}
