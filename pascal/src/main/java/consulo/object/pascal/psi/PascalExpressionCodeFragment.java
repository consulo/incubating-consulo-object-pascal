package consulo.object.pascal.psi;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
import com.siberika.idea.pascal.lang.parser.impl.PascalFileImpl;
import consulo.content.scope.SearchScope;
import consulo.language.ast.TokenType;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.impl.ast.FileElement;
import consulo.language.impl.file.SingleRootFileViewProvider;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.psi.PsiCodeFragment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class PascalExpressionCodeFragment extends PascalFileImpl implements PsiCodeFragment {
    private static final String NAME = "fragment.pas";

    @Nullable
    private final SmartPsiElementPointer<PsiElement> myContext;
    private SingleRootFileViewProvider myViewProvider;
    @Nullable
    private GlobalSearchScope myForcedResolveScope;
    private boolean myPhysical;

    public PascalExpressionCodeFragment(Project project, CharSequence text, @Nullable PsiElement context, boolean isPhysical) {
        this(new SingleRootFileViewProvider(PsiManager.getInstance(project), createFile(text, context), isPhysical), context, isPhysical);
    }

    private PascalExpressionCodeFragment(SingleRootFileViewProvider viewProvider, @Nullable PsiElement context, boolean isPhysical) {
        super(viewProvider);
        myContext = context != null ? SmartPointerManager.createPointer(context) : null;
        myViewProvider = viewProvider;
        myPhysical = isPhysical;
        viewProvider.forceCachedPsi(this);
        init(TokenType.CODE_FRAGMENT, PascalExpressionCodeFragmentElementType.INSTANCE);
    }

    public static PsiElement getPosition(PsiElement element) {
        if (element.getContainingFile() instanceof PascalExpressionCodeFragment fragment) {
            PsiElement context = fragment.getCodeFragmentContext();
            if (context != null) {
                return getPosition(context);
            }
        }
        return element;
    }

    @Nullable
    public PsiElement getCodeFragmentContext() {
        SmartPsiElementPointer<PsiElement> context = myContext;
        return context != null ? context.getElement() : null;
    }

    @Override
    public PsiElement getContext() {
        PsiElement context = getCodeFragmentContext();
        return context != null ? context : super.getContext();
    }

    @Override
    public SingleRootFileViewProvider getViewProvider() {
        SingleRootFileViewProvider viewProvider = myViewProvider;
        return viewProvider != null ? viewProvider : (SingleRootFileViewProvider) super.getViewProvider();
    }

    @Override
    public boolean isPhysical() {
        return myPhysical;
    }

    @Override
    public void forceResolveScope(GlobalSearchScope scope) {
        myForcedResolveScope = scope;
    }

    @Override
    public GlobalSearchScope getForcedResolveScope() {
        return myForcedResolveScope;
    }

    @Override
    public GlobalSearchScope getResolveScope() {
        if (myForcedResolveScope != null) {
            return myForcedResolveScope;
        }
        PsiElement context = getCodeFragmentContext();
        return context != null ? context.getResolveScope() : super.getResolveScope();
    }

    @Override
    public SearchScope getUseScope() {
        PsiElement context = getCodeFragmentContext();
        return context != null ? context.getUseScope() : super.getUseScope();
    }

    @Override
    public PsiFileImpl clone() {
        PascalExpressionCodeFragment clone = (PascalExpressionCodeFragment) cloneImpl((FileElement) calcTreeElement().clone());
        clone.myPhysical = false;
        clone.myOriginalFile = this;
        clone.myViewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(getProject()), createFile(getText(), getCodeFragmentContext()), false);
        clone.myViewProvider.forceCachedPsi(clone);
        return clone;
    }

    private static LightVirtualFile createFile(CharSequence text, @Nullable PsiElement context) {
        return context != null ? new ContextAwareVirtualFile(NAME, text, context) : new LightVirtualFile(NAME, PascalLanguage.INSTANCE, text);
    }
}
