package consulo.object.pascal.debugger;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.nativeDev.debugger.NativeDebuggerLanguageSupport;
import consulo.object.pascal.psi.PascalExpressionCodeFragment;
import consulo.project.Project;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class PascalNativeDebuggerLanguageSupport implements NativeDebuggerLanguageSupport {
    private static final String POINTER_PREFIX = "^";
    private static final String CLASS_OF_PREFIX = "class of ";

    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }

    @Override
    public String getDebuggerLanguage() {
        return "pascal";
    }

    @Override
    public PsiFile createExpressionCodeFragment(Project project, String text, @Nullable PsiElement context, boolean isPhysical) {
        return new PascalExpressionCodeFragment(project, text, context, isPhysical);
    }

    @Nullable
    @Override
    @RequiredReadAction
    public PsiElement findTypeDeclaration(Project project, PsiElement context, String typeName) {
        String type = typeName.trim();
        while (true) {
            if (type.startsWith(POINTER_PREFIX)) {
                type = type.substring(POINTER_PREFIX.length()).trim();
            }
            else if (type.regionMatches(true, 0, CLASS_OF_PREFIX, 0, CLASS_OF_PREFIX.length())) {
                type = type.substring(CLASS_OF_PREFIX.length()).trim();
            }
            else {
                break;
            }
        }
        if (type.isEmpty() || !type.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '_' || c == '.')) {
            return null;
        }
        return findDeclaration(project, context, type);
    }
}
