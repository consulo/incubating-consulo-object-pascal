package com.siberika.idea.pascal.editor;

import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.linemarker.PascalMarker;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.lang.search.Helper;
import com.siberika.idea.pascal.lang.search.UsedBy;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.colorScheme.EditorColorsManager;
import consulo.language.Language;
import consulo.language.editor.DaemonCodeAnalyzerSettings;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 05/09/2013
 */
@ExtensionImpl
public class PascalLineMarkerProvider implements LineMarkerProvider {

    public static final Logger LOG = Logger.getInstance(PascalLineMarkerProvider.class.getName());

    private final DaemonCodeAnalyzerSettings myDaemonSettings;
    private final EditorColorsManager myColorsManager;

    public PascalLineMarkerProvider() {
        myDaemonSettings = DaemonCodeAnalyzerSettings.getInstance();
        myColorsManager = EditorColorsManager.getInstance();
    }

    private void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super LineMarkerInfo> result) {
        boolean impl = true;
        PsiElement target = null;
        if (element instanceof PasExportedRoutineImpl) {
            target = SectionToggle.getRoutineTarget((PasExportedRoutineImpl) element);
        } else if (element instanceof PasRoutineImplDeclImpl) {
            target = SectionToggle.getRoutineTarget((PasRoutineImplDeclImpl) element);
            impl = false;
        } else if (element instanceof PasUsesClause) {
            target = SectionToggle.getUsesTarget((PasUsesClause) element);
            impl = PsiTreeUtil.getParentOfType(element, PasUnitInterface.class) != null;
        }
        if (PsiUtil.isElementUsable(target)) {
            result.add(createLineMarkerInfo(element, impl ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.ImplementingMethod, PascalMarker.SECTION_TOGGLE));
        }
        // Goto super
        if (element instanceof PascalNamedElement) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            if (GotoSuper.hasSuperTargets(namedElement.getNameIdentifier())) {
                result.add(createLineMarkerInfo(element, AllIcons.Gutter.OverridingMethod, PascalMarker.GOTO_SUPER));
            }
            if (element instanceof PasUnitModuleHead) {
                if (UsedBy.hasDependentModules((PasUnitModuleHead) element)) {
                    result.add(createLineMarkerInfo(element, PascalIcons.Idea.USED_BY, PascalMarker.USED_BY_UNIT));
                }
            }
        }
    }

    static LineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element, Image icon, @NotNull PascalMarker marker) {
        PsiElement el = getLeaf(element);
        return new LineMarkerInfo<>(el, el.getTextRange(),
                icon, Pass.LINE_MARKERS,
                marker.getTooltip(), marker.getHandler(),
                GutterIconRenderer.Alignment.RIGHT);
    }

    static PsiElement getLeaf(PsiElement element) {
        while (element.getFirstChild() != null) {
            element = element.getFirstChild();
        }
        return element;
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PasRoutineImplDeclImpl) {
            if (myDaemonSettings.SHOW_METHOD_SEPARATORS) {
                return LineMarkerInfo.createMethodSeparatorLineMarker(getLeaf(element), myColorsManager);
            }
        } else if ((element instanceof PasClassTypeDecl) || (element instanceof PasRecordDecl)) {
            if (Helper.hasHelpers((PascalStructType) element)) {
                return collectHelpers((PascalStructType) element);
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        for (PsiElement element : elements) {
            if ((element instanceof PascalRoutine) || (element instanceof PascalStructType) || (element instanceof PasUsesClause) || (element instanceof PasUnitModuleHead)) {
                collectNavigationMarkers(element, result);
            }
        }
    }

    private LineMarkerInfo collectHelpers(PascalStructType structType) {
        PsiElement leaf = getLeaf(structType);
        return new LineMarkerInfo<PsiElement>(leaf, leaf.getTextRange(),
                PascalIcons.HELPER, Pass.LINE_MARKERS, PascalMarker.HELPERS.getTooltip(), PascalMarker.HELPERS.getHandler(), GutterIconRenderer.Alignment.RIGHT) {
        };
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
