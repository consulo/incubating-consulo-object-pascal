package com.siberika.idea.pascal.editor;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalRoutineEntity;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.parameterInfo.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: George Bakhtadze
 * Date: 25/03/2015
 */
@ExtensionImpl
public class PascalParameterInfoHandler implements ParameterInfoHandler<PasCallExpr, PascalRoutineEntity> {
    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return null;
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(PascalRoutineEntity p, ParameterInfoContext context) {
        return null;
    }

    @Nullable
    @Override
    public PasCallExpr findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        PasCallExpr res = getCallExpr(context.getFile().findElementAt(context.getOffset()));
        context.setItemsToShow(getParameters(res));
        return res;
    }

    @Nullable
    @Override
    public PasCallExpr findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        PasCallExpr res = getCallExpr(context.getFile().findElementAt(context.getOffset()));
        if (res != null) {
            int index = ParameterInfoUtils.getCurrentParameterIndex(res.getArgumentList().getNode(), context.getOffset(), PasTypes.COMMA);
            context.setCurrentParameter(index);
        }
        return res;
    }

    private Object[] getParameters(PasCallExpr callExpr) {
        Map<String, PascalRoutineEntity> res = new TreeMap<String, PascalRoutineEntity>();
        for (PascalRoutineEntity routineEntity : PasReferenceUtil.resolveRoutines(callExpr)) {
            res.put(routineEntity.getName(), routineEntity);
        }
        return res.values().toArray();
    }

    @Override
    public void showParameterInfo(@NotNull PasCallExpr element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, element.getTextRange().getStartOffset(), this);
    }

    private PasCallExpr getCallExpr(PsiElement element) {
        PasCallExpr call = PsiTreeUtil.getParentOfType(element, PasCallExpr.class);
        if ((null == call) && (element != null)) {
            PsiElement prev = PsiTreeUtil.prevLeaf(element, true);
            if ((prev != null) && (prev.getText().equals("("))) {
                call = PsiTreeUtil.getParentOfType(prev, PasCallExpr.class);
            }
        }
        return call;
    }

    @Override
    public void updateParameterInfo(@NotNull PasCallExpr element, @NotNull UpdateParameterInfoContext context) {
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return ")";
    }

    @Override
    public boolean tracksParameterIndex() {
        return false;
    }

    @Override
    public void updateUI(PascalRoutineEntity p, @NotNull ParameterInfoUIContext context) {
        StringBuilder sb = new StringBuilder();
        int hlStart = -1;
        int hlEnd = -1;
        List<String> names = p.getFormalParameterNames();
        List<String> defaultValues = p.getFormalParameterDefaultValues();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String type = p.getFormalParameterTypes().get(i);
            ParamModifier access = p.getFormalParameterAccess().get(i);

            if (sb.length() > 0) {
                sb.append("; ");
            }

            if (i == context.getCurrentParameterIndex()) {
                hlStart = sb.length();
            }
            if (access != ParamModifier.NONE) {
                sb.append(access.name().toLowerCase()).append(" ");
            }
            sb.append(name).append(": ").append(type);
            if (i == context.getCurrentParameterIndex()) {
                hlEnd = sb.length();
            }
            if (i >= names.size() - defaultValues.size()) {
                int idx = i - (names.size() - defaultValues.size());
                sb.append(" = ").append(defaultValues.get(idx));
            }
        }
        boolean isDisabled = context.getCurrentParameterIndex() >= names.size();
        context.setupUIComponentPresentation(sb.toString(), hlStart, hlEnd, isDisabled, false, false, context.getDefaultParameterColor()
        );
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
