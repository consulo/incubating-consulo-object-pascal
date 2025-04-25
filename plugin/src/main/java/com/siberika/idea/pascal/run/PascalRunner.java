package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.module.PascalModuleType;
import consulo.annotation.component.ExtensionImpl;
import consulo.compiler.ModuleCompilerPathsManager;
import consulo.execution.configuration.RunProfile;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.module.Module;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Author: George Bakhtadze
 * Date: 05/12/2012
 */
@ExtensionImpl
public class PascalRunner extends DefaultProgramRunner {
    @Override
    @NotNull
    public String getRunnerId() {
        return "com.siberika.idea.pascal.run.PascalRunner";
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return (profile instanceof PascalRunConfiguration) && executorId.equals(DefaultRunExecutor.EXECUTOR_ID);
    }

    @Nullable
    public static String getExecutable(@NotNull Module module, String programFileName) {
        if (null == programFileName) {
            return null;
        }
        String path = PascalModuleType.getExeOutputPath(module);
        if (StringUtil.isEmpty(path)) {
            ModuleCompilerPathsManager compilerModuleExtension = ModuleCompilerPathsManager.getInstance(module);
            path = VirtualFileUtil.urlToPath(compilerModuleExtension.getCompilerOutputUrl(ProductionContentFolderTypeProvider.getInstance()));
        }
        return FileUtil.getExecutable(new File(path != null ? path : ""), programFileName).getPath();
    }
}
