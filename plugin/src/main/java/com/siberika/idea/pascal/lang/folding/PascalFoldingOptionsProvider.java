package com.siberika.idea.pascal.lang.folding;

import com.siberika.idea.pascal.PascalBundle;
import consulo.options.SimpleConfigurableByProperties;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;

import javax.annotation.Nonnull;

public class PascalFoldingOptionsProvider extends SimpleConfigurableByProperties {
    @RequiredUIAccess
    @Nonnull
    @Override
    protected Component createLayout(PropertyBuilder propertyBuilder) {
        VerticalLayout root = VerticalLayout.create();
        PascalCodeFoldingSettings settings = PascalCodeFoldingSettings.getInstance();

        CheckBox collapseEnums = CheckBox.create(PascalBundle.message("ui.settings.folding.collapse.enums"));
        root.add(collapseEnums);
        propertyBuilder.add(collapseEnums, settings::isCollapseEnums, settings::setCollapseEnums);

        CheckBox showQualifiedIdentifiers = CheckBox.create(PascalBundle.message("ui.settings.folding.with"));
        root.add(showQualifiedIdentifiers);
        propertyBuilder.add(showQualifiedIdentifiers, settings::isFoldWithBlocks, settings::setFoldWithBlocks);

        return root;
    }
}