package consulo.object.pascal.moduleAware;

import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.ModuleAwareIndexOptions;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;

import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public final class PascalDefineEnv {
    public static final String PROVIDER_ID = "pascal-defines";

    private PascalDefineEnv() {
    }

    public static PascalDefineOptions optionsFor(PsiElement context) {
        return read(() -> ModuleAwareIndexOptions.getOptions(context, PROVIDER_ID, PascalDefineOptionsExternalizer.INSTANCE));
    }

    public static PascalDefineOptions optionsFor(Project project, VirtualFile file) {
        return read(() -> ModuleAwareIndexOptions.getOptions(project, file, PROVIDER_ID, PascalDefineOptionsExternalizer.INSTANCE));
    }

    private static PascalDefineOptions read(Supplier<PascalDefineOptions> reader) {
        PascalDefineOptions options;
        try {
            options = reader.get();
        }
        catch (IllegalArgumentException serviceNotBound) {
            return PascalDefineOptions.EMPTY;
        }
        return options == null ? PascalDefineOptions.EMPTY : options;
    }
}
