package com.siberika.idea.pascal.lang.folding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.siberika.idea.pascal.PascalBundle;
import consulo.options.SimpleConfigurableByProperties;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

public class PascalFoldingOptionsProvider extends SimpleConfigurableByProperties  {

    protected PascalFoldingOptionsProvider() {
        super(PascalCodeFoldingSettings.getInstance());
        PascalCodeFoldingSettings settings = getInstance();

        checkBox(PascalBundle.message("ui.settings.folding.collapse.enums"), settings::isCollapseEnums, settings::setCollapseEnums);
        checkBox(PascalBundle.message("ui.settings.folding.with"), settings::isFoldWithBlocks, settings::setFoldWithBlocks);
    }

    @RequiredUIAccess
    @Nonnull
    @Override
    protected Component createLayout(PropertyBuilder propertyBuilder) {
        return null;
    }
}
