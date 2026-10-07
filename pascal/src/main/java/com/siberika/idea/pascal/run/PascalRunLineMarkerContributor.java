package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.PascalLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.lineMarker.ExecutorAction;
import consulo.execution.lineMarker.RunLineMarkerContributor;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Author: George Bakhtadze
 * Date: 09/07/2016
 */
@ExtensionImpl
public class PascalRunLineMarkerContributor extends RunLineMarkerContributor {

    private static final Function<PsiElement, String> TOOLTIP_PROVIDER = element -> "Run Program";

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (PascalRunContextConfigurationProducer.isProgramLeafElement(element)) {
            return new Info(PascalIcons.Idea.RUN, TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
        }
        return null;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
