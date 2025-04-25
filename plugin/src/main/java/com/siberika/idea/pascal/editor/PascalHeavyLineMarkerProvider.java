package com.siberika.idea.pascal.editor;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.linemarker.PascalMarker;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.search.PascalDefinitionsSearch;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.application.util.concurrent.JobLauncher;
import consulo.application.util.function.Computable;
import consulo.application.util.function.Processor;
import consulo.language.Language;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 01/08/2015
 */
@ExtensionImpl
public class PascalHeavyLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        ApplicationManager.getApplication().assertReadAccessAllowed();
        List<Computable<List<LineMarkerInfo>>> tasks = new ArrayList<>();
        Processor<? super PasEntityScope> consumer = (Processor<PasEntityScope>) descending -> false;
        for (PsiElement element : elements) {
            if (PsiUtil.isElementUsable(element) && element instanceof PasEntityScope) {
                tasks.add(new Computable<List<LineMarkerInfo>>() {
                    @Override
                    public List<LineMarkerInfo> compute() {
                        boolean noMarker = true;
                        if (element instanceof PascalStructType) {
                            noMarker = PascalDefinitionsSearch.findImplementations(((PascalNamedElement) element).getNameIdentifier(), consumer);
                        } else if ((element instanceof PasExportedRoutine) || (element instanceof PasRoutineImplDecl)) {
                            PasEntityScope scope = ((PasEntityScope) element).getContainingScope();
                            if (scope instanceof PascalStructType) {
                                noMarker = PascalDefinitionsSearch.findImplementingMethods((PascalRoutine) element, consumer);
                            }
                        }
                        if (noMarker) {
                            return Collections.emptyList();
                        } else {
                            return Collections.singletonList(PascalLineMarkerProvider.createLineMarkerInfo(element, AllIcons.Gutter.OverridenMethod, PascalMarker.DESCENDING_ENTITIES));
                        }
                    }
                });
            }
        }
        Object lock = new Object();
        ProgressIndicator indicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
        JobLauncher.getInstance().invokeConcurrentlyUnderProgress(tasks, indicator, true, true, computable -> {
            List<LineMarkerInfo> infos = computable.compute();
            synchronized (lock) {
                result.addAll(infos);
            }
            return true;
        });
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
