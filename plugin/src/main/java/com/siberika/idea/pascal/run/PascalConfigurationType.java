package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.PascalIcons;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.execution.configuration.*;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
@ExtensionImpl(id = "FPC")
public class PascalConfigurationType implements ConfigurationType, DumbAware {
    private final ConfigurationFactory myFactory;

    public PascalConfigurationType() {
        myFactory = new ConfigurationFactory(this) {
            @NotNull
            @Override
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new PascalRunConfiguration(getDisplayName().get(), new RunConfigurationModule(project), this);
            }
        };
    }

    @NotNull
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Pascal");
    }

    public LocalizeValue getConfigurationTypeDescription() {
        return LocalizeValue.localizeTODO("Pascal run configuration");
    }

    public Image getIcon() {
        return PascalIcons.GENERAL;
    }

    @NotNull
    public String getId() {
        return "#com.siberika.idea.pascal.run.PascalConfigurationType";
    }

    public static PascalConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(PascalConfigurationType.class);
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }
}
