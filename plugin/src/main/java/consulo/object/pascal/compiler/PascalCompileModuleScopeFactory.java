package consulo.object.pascal.compiler;

import consulo.annotation.component.ExtensionImpl;
import consulo.compiler.scope.CompileModuleScopeFactory;
import consulo.compiler.scope.FileIndexCompileScope;
import consulo.compiler.scope.ModuleRootCompileScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12/07/2021
 */
@ExtensionImpl
public class PascalCompileModuleScopeFactory implements CompileModuleScopeFactory {
    @Nullable
    @Override
    public FileIndexCompileScope createScope(@Nonnull Module module, boolean b, boolean b1) {
        ObjectPascalModuleExtension extension = ModuleUtilCore.getExtension(module, ObjectPascalModuleExtension.class);
        if (extension != null) {
            return new ModuleRootCompileScope(module, b);
        }
        return null;
    }
}
