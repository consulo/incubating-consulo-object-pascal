package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.module.ModuleService;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.util.ModuleUtil;
import consulo.content.bundle.Sdk;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.virtualFileSystem.BinaryFileDecompiler;
import consulo.virtualFileSystem.VirtualFile;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
abstract class PascalUnitDecompiler implements BinaryFileDecompiler {

    abstract PascalCachingUnitDecompiler createCache(Module module, Sdk sdk);

    String doDecompile(VirtualFile file) {
        final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return "";
        final Project project = projects[0];
        ModuleService.ensureNameFileCache(file, project, true);     // Do not check TTL to avoid reentrant indexing errors
        Module module = ModuleUtil.getModuleForLibraryFile(project, file);
        if (null == module) {
            return PascalBundle.message("decompile.no.module", file.getPath());
        }
        return decompile(module, file);
    }

    private String decompile(Module module, VirtualFile file) {
        Sdk sdk = ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class);
        if (null == sdk) {
            return PascalBundle.message("decompile.wrong.sdk");
        }
        PascalCachingUnitDecompiler decompilerCache;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (sdk) {
            decompilerCache = (PascalCachingUnitDecompiler) BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey());
            if (null == decompilerCache) {
                decompilerCache = createCache(module, sdk);
                BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey(), decompilerCache);
            }
        }
        return decompilerCache.getSource(file);
    }

}
