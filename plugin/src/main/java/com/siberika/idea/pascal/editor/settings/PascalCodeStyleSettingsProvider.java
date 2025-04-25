package com.siberika.idea.pascal.editor.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.Configurable;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.CustomCodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2017
 */
@ExtensionImpl
public class PascalCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public String getConfigurableDisplayName() {
        return "Pascal";
    }

    @NotNull
    @Override
    public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
        return new PascalCodeStyleConfigurable(settings, originalSettings);
    }

    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new PascalCodeStyleSettings(settings);
    }
}
