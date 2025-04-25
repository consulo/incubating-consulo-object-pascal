package com.siberika.idea.pascal.editor.linemarker;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.util.EditorUtil;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.application.util.query.Query;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.ui.PsiElementListNavigator;
import consulo.language.editor.ui.navigation.BackgroundUpdaterTask;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiElement;
import consulo.project.DumbService;
import consulo.util.collection.SmartHashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Set;

public abstract class CollectionNavigationHandler<T extends PsiElement> implements GutterIconNavigationHandler<PsiElement> {

    private final String titleMsg;
    private final String searchTitleMsg;
    private final String noItemsMsg;
    private final String impossibleReindexMsg;
    private final boolean navigateIfSingleResult;

    CollectionNavigationHandler(boolean navigateIfSingleResult, String titleMsg, String searchTitleMsg, String noItemsMsg, String impossibleReindexMsg) {
        this.titleMsg = titleMsg;
        this.searchTitleMsg = searchTitleMsg;
        this.noItemsMsg = noItemsMsg;
        this.impossibleReindexMsg = impossibleReindexMsg;
        this.navigateIfSingleResult = navigateIfSingleResult;
    }

    abstract Query<T> createQuery(PsiElement element);

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {
        if (DumbService.isDumb(elt.getProject())) {
            DumbService.getInstance(elt.getProject()).showDumbModeNotification(impossibleReindexMsg);
            return;
        }
        NavigatablePsiElement[] targetsNav = new NavigatablePsiElement[1];
        if (!ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            createQuery(elt).forEach(new Processor<T>() {
                        @Override
                        public boolean process(T element) {
                            targetsNav[0] = (NavigatablePsiElement) element;
                            return false;
                        }
                    });
        }, searchTitleMsg, true, elt.getProject(), (JComponent) e.getComponent())) {
            return;
        }
        if (targetsNav[0] == null) {
            return;
        }

        BackgroundUpdaterTask updater = new TargetsUpdater(elt, navigateIfSingleResult);

        PsiElementListNavigator.openTargets(e, targetsNav, searchTitleMsg,searchTitleMsg, new EditorUtil.MyPsiElementCellRenderer(), updater);
    }

    private class TargetsUpdater extends BackgroundUpdaterTask {
        @NotNull
        private final PsiElement element;
        private final boolean navigateIfSingleResult;

        private final Set<PsiElement> processed;

        public TargetsUpdater(@NotNull PsiElement element, boolean navigateIfSingleResult) {
            super(element.getProject(), searchTitleMsg, null);
            this.element = element;
            this.navigateIfSingleResult = navigateIfSingleResult;
            this.processed = new SmartHashSet<>();
        }

        @Override
        public String getCaption(int size) {
            return String.format("%s (%d %s)", titleMsg, size, PascalBundle.message("navigate.status.found"));
        }

        @Override
        public void onSuccess() {
            super.onSuccess();
            if (!navigateIfSingleResult) {
                return;
            }
            PsiElement oneElement = getTheOnlyOneElement();
            if (oneElement instanceof NavigatablePsiElement) {
                ((NavigatablePsiElement)oneElement).navigate(true);
                myPopup.cancel();
            }
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            super.run(indicator);
            createQuery(element).forEach(new Processor<PsiElement>() {
                @Override
                public boolean process(PsiElement element) {
                    if (!processed.contains(element)) {
                        processed.add(element);
                        if (!updateComponent(element)) {
                            indicator.cancel();
                        }
                    }
                    ProgressManager.checkCanceled();
                    return true;
                }
            });
        }
    }
}
