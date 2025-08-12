package com.siberika.idea.pascal.editor.settings;

import com.siberika.idea.pascal.PascalLanguage;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractConfigurable;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractPanel;
import consulo.language.codeStyle.ui.setting.TabbedLanguageCodeStylePanel;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2017
 */
public class PascalCodeStyleConfigurable extends CodeStyleAbstractConfigurable {
    public PascalCodeStyleConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
        super(settings, cloneSettings, ObjectPascalLocalize.colorSettingsName());
    }

    @Override
    protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
        return new PascalCodeStyleMainPanel(getCurrentSettings(), settings);
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    private static class PascalCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
        private PascalCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(PascalLanguage.INSTANCE, currentSettings, settings);
        }
    }
}
