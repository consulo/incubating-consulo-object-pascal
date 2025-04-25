package com.siberika.idea.pascal.module;

import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 08/01/2013
 */
@Deprecated
public class PascalModuleType {
    public static boolean isPascalModule(Module module) {
        return module != null && ModuleUtilCore.getExtension(module, ObjectPascalModuleExtension.class) != null;
    }

    @Nullable
    public static VirtualFile getMainFile(Module module) {
        ObjectPascalModuleExtension extension = ModuleUtilCore.getExtension(module, ObjectPascalModuleExtension.class);
        if (extension != null) {
            String mainFile = extension.getMainFilePath();
            if (!StringUtil.isEmpty(mainFile)) {
                return LocalFileSystem.getInstance().findFileByPath(mainFile);
            }
        }

        return null;
    }

    public static void setMainFile(Module module, VirtualFile file) {
        //module.putUserData(USERDATA_KEY_MAIN_FILE, file);
//        if (file != null) {
//            module.setOption(JpsPascalModuleType.USERDATA_KEY_MAIN_FILE.toString(), file.getPath());
//        }
    }

    @Nullable
    public static String getExeOutputPath(Module module) {
        ObjectPascalModuleExtension extension = ModuleUtilCore.getExtension(module, ObjectPascalModuleExtension.class);
        if (extension != null) {
            return extension.getOutputPath();
        }
        return null;
    }

    public static void setExeOutputPath(Module module, String path) {
        //module.setOption(JpsPascalModuleType.USERDATA_KEY_EXE_OUTPUT_PATH.toString(), path);
    }

}
