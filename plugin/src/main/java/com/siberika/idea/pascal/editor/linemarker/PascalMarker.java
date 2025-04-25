package com.siberika.idea.pascal.editor.linemarker;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.search.DescendingEntities;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.lang.search.Helper;
import com.siberika.idea.pascal.lang.search.UsedBy;
import com.siberika.idea.pascal.util.EditorUtil;
import consulo.application.ReadAction;
import consulo.application.util.query.EmptyQuery;
import consulo.application.util.query.Query;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.Collections;
import java.util.function.Function;

public class PascalMarker {
    private final GutterIconNavigationHandler<PsiElement> handler;
    private final Function<PsiElement, String> tooltip;

    private PascalMarker(GutterIconNavigationHandler<PsiElement> handler, Function<PsiElement, String> tooltip) {
        this.handler = handler;
        this.tooltip = tooltip;
    }

    public static PascalMarker SECTION_TOGGLE = new PascalMarker(
            (e, elt) -> {
                PsiElement target = null;
                if (elt.getParent() instanceof PascalRoutine) {
                    target = SectionToggle.getRoutineTarget((PascalRoutine) elt.getParent());
                } else if (elt.getParent() instanceof PasUsesClause) {
                    target = SectionToggle.getUsesTarget((PasUsesClause) elt.getParent());
                }
                if (target != null) {
                    EditorUtil.navigateTo(e, PascalBundle.message("navigate.title.toggle.section"), null, Collections.singletonList(target));
                }
            },
            psiElement -> PascalBundle.message("navigate.title.toggle.section")
    );

    public static PascalMarker GOTO_SUPER = new PascalMarker(
            new CollectionNavigationHandler<PasEntityScope>(true,
                    PascalBundle.message("navigate.title.goto.super"),
                    PascalBundle.message("navigate.title.goto.super.search"),
                    PascalBundle.message("navigate.info.goto.super.noitems"),
                    PascalBundle.message("navigate.goto.super.impossible.reindex")) {

                @Override
                Query<PasEntityScope> createQuery(PsiElement element) {
                    return GotoSuper.search(element);
                }
            },
            psiElement -> PascalBundle.message("navigate.title.goto.super")
    );

    public static PascalMarker DESCENDING_ENTITIES = new PascalMarker(
            new CollectionNavigationHandler<PsiElement>(true,
                    PascalBundle.message("navigate.title.goto.subclassed"),
                    PascalBundle.message("navigate.title.goto.subclassed.search"),
                    PascalBundle.message("navigate.info.subclassed.noitems"),
                    PascalBundle.message("navigate.subclassed.impossible.reindex")) {

                @Override
                Query<PsiElement> createQuery(PsiElement element) {
                    return DescendingEntities.getQuery(element, GlobalSearchScope.allScope(PsiUtilCore.getProjectInReadAction(element)));
                }
            },
            psiElement -> PascalBundle.message("navigate.title.goto.subclassed")
    );

    private static final EmptyQuery<PascalModule> USED_BY_EMPTY_QUERY = new EmptyQuery<>();
    
    public static PascalMarker USED_BY_UNIT = new PascalMarker(
            new CollectionNavigationHandler<PascalModule>(false,
                    PascalBundle.message("navigate.title.used.by"),
                    PascalBundle.message("navigate.title.used.by.search"),
                    PascalBundle.message("navigate.info.used.by.noitems"),
                    PascalBundle.message("navigate.used.by.impossible.reindex")) {

                @Override
                Query<PascalModule> createQuery(PsiElement element) {
                    PasUnitModuleHead moduleHead = ReadAction.compute(() -> PsiTreeUtil.getParentOfType(element, PasUnitModuleHead.class));
                    return moduleHead != null ? UsedBy.getQuery(moduleHead) : USED_BY_EMPTY_QUERY;
                }
            },
            psiElement -> PascalBundle.message("navigate.title.used.by")
    );

    private static final EmptyQuery<PascalStructType> HELPERS_EMPTY_QUERY = new EmptyQuery<>();

    public static PascalMarker HELPERS = new PascalMarker(
            new CollectionNavigationHandler<PascalStructType>(false,
                    PascalBundle.message("navigate.title.goto.helper"),
                    PascalBundle.message("navigate.title.goto.helper.search"),
                    PascalBundle.message("navigate.info.helper.noitems"),
                    PascalBundle.message("navigate.helper.impossible.reindex")) {

                @Override
                Query<PascalStructType> createQuery(PsiElement element) {
                    if (element.getParent() instanceof PascalStructType) {
                        return Helper.getQuery((PascalStructType) element.getParent());
                    } else {
                        return HELPERS_EMPTY_QUERY;
                    }
                }
            },
            psiElement -> PascalBundle.message("navigate.title.goto.helper")
    );

    public GutterIconNavigationHandler<PsiElement> getHandler() {
        return handler;
    }

    public Function<PsiElement, String> getTooltip() {
        return tooltip;
    }
}
