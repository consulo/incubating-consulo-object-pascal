package com.siberika.idea.pascal.ide.actions;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.context.CodePlace;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.stub.PascalUnitSymbolIndex;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.dataContext.DataManager;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import jakarta.annotation.Nonnull;

import java.util.Collections;

import static consulo.ui.ex.action.ActionPlaces.EDITOR_POPUP;

/**
 * @author George Bakhtadze
 * @since 2015-12-21
 */
public class UsesActions {
    public static class AddUnitAction extends BaseUsesAction {
        private final String unitName;
        private final boolean toInterface;

        public AddUnitAction(@Nonnull LocalizeValue name, String unitName, boolean toInterface) {
            super(name);
            this.unitName = unitName;
            this.toInterface = toInterface;
        }

        @Override
        public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            PascalImportOptimizer.addUnitToSection(PsiUtil.getElementPasModule(file), Collections.singletonList(unitName), toInterface);
        }
    }

    public static class NewUnitAction extends BaseUsesAction {
        private final String unitName;

        public NewUnitAction(@Nonnull LocalizeValue name, String unitName) {
            super(name);
            this.unitName = unitName;
        }

        @Override
        public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            final DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
            final CreateModuleAction act = new CreateModuleAction();
            final AnActionEvent ev = AnActionEvent.createFromAnAction(act, null, EDITOR_POPUP, dataContext);
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    act.actionPerformed(ev);
                }
            });
        }
    }

    public static class SearchUnitAction extends BaseUsesAction implements SyntheticIntentionAction {
        private final boolean toInterface;
        private final PascalNamedElement namedElement;
        private String unitName;

        public SearchUnitAction(PascalNamedElement namedElement, boolean toInterface) {
            super(ObjectPascalLocalize.actionUnitSearch(namedElement.getName()));
            this.namedElement = namedElement;
            this.toInterface = toInterface;
        }

        @Override
        public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            if (unitName != null) {
                PascalImportOptimizer.addUnitToSection(PsiUtil.getElementPasModule(file), Collections.singletonList(unitName), toInterface);
                EditorUtil.showInformationHint(
                    editor,
                    ObjectPascalLocalize.actionUnitSearchAdded(
                        unitName,
                        toInterface
                            ? ObjectPascalLocalize.unitSectionInterface()
                            : ObjectPascalLocalize.unitSectionImplementation()
                    )
                );
            }
        }

        @Override
        public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
            Context context = new Context(namedElement, namedElement, file);
            if ((file != null) && PascalLanguage.INSTANCE.equals(file.getLanguage()) && context.contains(CodePlace.FIRST_IN_NAME)) {
                lazyInit();
                return unitName != null;
            }
            else {
                return false;
            }
        }

        @Nonnull
        @Override
        public LocalizeValue getText() {
            lazyInit();
            return unitName != null
                ? ObjectPascalLocalize.actionUnitSearchFound(unitName)
                : ObjectPascalLocalize.actionUnitSearchNotfound(namedElement.getName());
        }

        synchronized private void lazyInit() {
            Module module = ModuleUtilCore.findModuleForPsiElement(namedElement);
            final GlobalSearchScope scope = module != null
                ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false)
                : GlobalSearchScope.allScope(namedElement.getProject());
            String searchFor = namedElement.getName();
            String searchForUpper = searchFor.toUpperCase();
            for (PascalNamedElement element : StubIndex.getElements(
                PascalUnitSymbolIndex.KEY,
                searchForUpper,
                namedElement.getProject(),
                scope,
                PascalNamedElement.class
            )) {
                String name = element.getName();
                if ((element instanceof PascalStubElement) &&
                    (searchFor.equalsIgnoreCase(element.getName())
                        || (name.toUpperCase().startsWith(searchForUpper) && (element instanceof PascalRoutine)))) {
                    PsiElement affScope = PsiUtil.retrieveElementScope(element);
                    String uName = ((PascalStubElement) element).getContainingUnitName();
                    if ((uName != null) && (affScope instanceof PasModule)
                        && (((PasModule) affScope).getModuleType() == PascalModule.ModuleType.UNIT)) {
                        unitName = uName;
                        break;
                    }
                }
            }
        }
    }

    private static abstract class BaseUsesAction extends BaseIntentionAction {
        @Nonnull
        private final LocalizeValue myName;

        private BaseUsesAction(@Nonnull LocalizeValue name) {
            this.myName = name;
        }

        @Override
        public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
            return (file != null) && PascalLanguage.INSTANCE.equals(file.getLanguage());
        }

        @Nonnull
        @Override
        public LocalizeValue getText() {
            return myName;
        }
    }
}
