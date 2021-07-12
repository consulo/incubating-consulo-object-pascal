package consulo.object.pascal.compiler;

import com.intellij.compiler.impl.FileIndexCompileScope;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import consulo.compiler.impl.CompileModuleScopeFactory;
import consulo.compiler.impl.ModuleRootCompileScope;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12/07/2021
 */
public class PascalCompileModuleScopeFactory implements CompileModuleScopeFactory {
    @Nullable
    @Override
    public FileIndexCompileScope createScope(@Nonnull Module module, boolean b) {
        ObjectPascalModuleExtension extension = ModuleUtilCore.getExtension(module, ObjectPascalModuleExtension.class);
        if (extension != null) {
            return new ModuleRootCompileScope(module, b);
        }
        return null;
    }
}
