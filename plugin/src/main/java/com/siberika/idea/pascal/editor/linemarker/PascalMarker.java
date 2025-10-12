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
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;

import java.util.Collections;
import java.util.function.Function;

public class PascalMarker {
    private final GutterIconNavigationHandler<PsiElement> handler;
    private final Function<PsiElement, LocalizeValue> tooltip;

    private PascalMarker(GutterIconNavigationHandler<PsiElement> handler, Function<PsiElement, LocalizeValue> tooltip) {
        this.handler = handler;
        this.tooltip = tooltip;
    }

    public static PascalMarker SECTION_TOGGLE = new PascalMarker(
        (e, elt) -> {
            PsiElement target = null;
            if (elt.getParent() instanceof PascalRoutine) {
                target = SectionToggle.getRoutineTarget((PascalRoutine) elt.getParent());
            }
            else if (elt.getParent() instanceof PasUsesClause) {
                target = SectionToggle.getUsesTarget((PasUsesClause) elt.getParent());
            }
            if (target != null) {
                EditorUtil.navigateTo(
                    e,
                    ObjectPascalLocalize.navigateTitleToggleSection(),
                    LocalizeValue.empty(),
                    Collections.singletonList(target)
                );
            }
        },
        psiElement -> ObjectPascalLocalize.navigateTitleToggleSection()
    );

    public static PascalMarker GOTO_SUPER = new PascalMarker(
        new CollectionNavigationHandler<PasEntityScope>(
            true,
            ObjectPascalLocalize.navigateTitleGotoSuper().get(),
            ObjectPascalLocalize.navigateTitleGotoSuperSearch().get(),
            ObjectPascalLocalize.navigateInfoGotoSuperNoitems().get(),
            ObjectPascalLocalize.navigateGotoSuperImpossibleReindex().get()
        ) {

            @Override
            Query<PasEntityScope> createQuery(PsiElement element) {
                return GotoSuper.search(element);
            }
        },
        psiElement -> ObjectPascalLocalize.navigateTitleGotoSuper()
    );

    public static PascalMarker DESCENDING_ENTITIES = new PascalMarker(
        new CollectionNavigationHandler<PsiElement>(
            true,
            ObjectPascalLocalize.navigateTitleGotoSubclassed().get(),
            ObjectPascalLocalize.navigateTitleGotoSubclassedSearch().get(),
            ObjectPascalLocalize.navigateInfoSubclassedNoitems().get(),
            ObjectPascalLocalize.navigateSubclassedImpossibleReindex().get()
        ) {

            @Override
            Query<PsiElement> createQuery(PsiElement element) {
                return DescendingEntities.getQuery(element, GlobalSearchScope.allScope(PsiUtilCore.getProjectInReadAction(element)));
            }
        },
        psiElement -> ObjectPascalLocalize.navigateTitleGotoSubclassed()
    );

    private static final EmptyQuery<PascalModule> USED_BY_EMPTY_QUERY = new EmptyQuery<>();

    public static PascalMarker USED_BY_UNIT = new PascalMarker(
        new CollectionNavigationHandler<PascalModule>(
            false,
            ObjectPascalLocalize.navigateTitleUsedBy().get(),
            ObjectPascalLocalize.navigateTitleUsedBySearch().get(),
            ObjectPascalLocalize.navigateInfoUsedByNoitems().get(),
            ObjectPascalLocalize.navigateUsedByImpossibleReindex().get()
        ) {

            @Override
            Query<PascalModule> createQuery(PsiElement element) {
                PasUnitModuleHead moduleHead = ReadAction.compute(() -> PsiTreeUtil.getParentOfType(element, PasUnitModuleHead.class));
                return moduleHead != null ? UsedBy.getQuery(moduleHead) : USED_BY_EMPTY_QUERY;
            }
        },
        psiElement -> ObjectPascalLocalize.navigateTitleUsedBy()
    );

    private static final EmptyQuery<PascalStructType> HELPERS_EMPTY_QUERY = new EmptyQuery<>();

    public static PascalMarker HELPERS = new PascalMarker(
        new CollectionNavigationHandler<PascalStructType>(
            false,
            ObjectPascalLocalize.navigateTitleGotoHelper().get(),
            ObjectPascalLocalize.navigateTitleGotoHelperSearch().get(),
            ObjectPascalLocalize.navigateInfoHelperNoitems().get(),
            ObjectPascalLocalize.navigateHelperImpossibleReindex().get()
        ) {

            @Override
            Query<PascalStructType> createQuery(PsiElement element) {
                if (element.getParent() instanceof PascalStructType) {
                    return Helper.getQuery((PascalStructType) element.getParent());
                }
                else {
                    return HELPERS_EMPTY_QUERY;
                }
            }
        },
        psiElement -> ObjectPascalLocalize.navigateTitleGotoHelper()
    );

    public GutterIconNavigationHandler<PsiElement> getHandler() {
        return handler;
    }

    public Function<PsiElement, LocalizeValue> getTooltip() {
        return tooltip;
    }
}
