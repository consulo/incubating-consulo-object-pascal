package com.siberika.idea.pascal.editor.linemarker;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.util.EditorUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.util.query.Query;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.ui.navigation.ItemWithPresentation;
import consulo.language.editor.ui.navigation.PsiTargetNavigationService;
import consulo.language.editor.ui.navigation.TargetUpdaterTask;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.DumbService;
import consulo.ui.event.ComponentEvent;
import consulo.util.collection.SmartHashSet;
import consulo.util.concurrent.coroutine.CoroutineStep;
import consulo.util.concurrent.coroutine.step.CodeExecution;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class CollectionNavigationHandler<T extends PsiElement> implements GutterIconNavigationHandler<PsiElement> {
    private final LocalizeValue titleMsg;
    private final LocalizeValue searchTitleMsg;
    private final LocalizeValue noItemsMsg;
    private final LocalizeValue impossibleReindexMsg;
    private final boolean navigateIfSingleResult;

    CollectionNavigationHandler(
        boolean navigateIfSingleResult,
        LocalizeValue titleMsg,
        LocalizeValue searchTitleMsg,
        LocalizeValue noItemsMsg,
        LocalizeValue impossibleReindexMsg
    ) {
        this.titleMsg = titleMsg;
        this.searchTitleMsg = searchTitleMsg;
        this.noItemsMsg = noItemsMsg;
        this.impossibleReindexMsg = impossibleReindexMsg;
        this.navigateIfSingleResult = navigateIfSingleResult;
    }

    @RequiredReadAction
    abstract Query<T> createQuery(PsiElement element);

    @Override
    public void navigate(ComponentEvent<?> e, PsiElement elt) {
        if (DumbService.isDumb(elt.getProject())) {
            DumbService.getInstance(elt.getProject()).showDumbModeNotification(impossibleReindexMsg);
            return;
        }

        CoroutineStep<Void, Collection<PsiElement>> prefetch = CodeExecution.supply(() -> {
            List<PsiElement> first = new ArrayList<>(1);
            ReadAction.compute(() -> createQuery(elt)).forEach(element -> {
                if (element instanceof NavigatablePsiElement navigatable) {
                    first.add(navigatable);
                }
                return false;
            });
            return first;
        });

        Application.get().getInstance(PsiTargetNavigationService.class)
            .newNavigator(prefetch)
            .presentationProvider(EditorUtil.PASCAL_PRESENTATION)
            .title(titleMsg)
            .findUsagesTitle(searchTitleMsg)
            .emptyText(noItemsMsg)
            .updater(new TargetsUpdater(elt, navigateIfSingleResult))
            .navigate(e, elt.getProject());
    }

    private class TargetsUpdater extends TargetUpdaterTask<PsiElement> {
        private final PsiElement element;
        private final boolean navigateIfSingleResult;
        private final Set<PsiElement> processed;

        public TargetsUpdater(@Nonnull PsiElement element, boolean navigateIfSingleResult) {
            super(element.getProject(), searchTitleMsg, EditorUtil.PASCAL_PRESENTATION);
            this.element = element;
            this.navigateIfSingleResult = navigateIfSingleResult;
            this.processed = new SmartHashSet<>();
        }

        @Nonnull
        @Override
        public LocalizeValue getCaption(int size) {
            return LocalizeValue.of(String.format(
                "%s (%d %s)",
                titleMsg.get(),
                size,
                ObjectPascalLocalize.navigateStatusFound().get()
            ));
        }

        @Override
        public void onSuccess() {
            super.onSuccess();
            if (!navigateIfSingleResult) {
                return;
            }
            ItemWithPresentation<PsiElement> only = getTheOnlyOneElement();
            PsiElement oneElement = only == null ? null : only.dereference();
            if (oneElement instanceof NavigatablePsiElement navigatable) {
                navigatable.navigate(true);
                myPopup.cancel();
            }
        }

        @Override
        public void run(@Nonnull ProgressIndicator indicator) {
            super.run(indicator);
            ReadAction.compute(() -> createQuery(element)).forEach(found -> {
                if (processed.add(found) && !updateElement(found)) {
                    indicator.cancel();
                }
                ProgressManager.checkCanceled();
                return true;
            });
        }
    }
}
