package consulo.object.pascal.lazarus.importing;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurableFactory;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.object.pascal.lazarus.setting.LazarusProjectSettings;
import consulo.object.pascal.lazarus.setting.LazarusSettings;
import consulo.project.ProjectManager;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusSettingsConfigurableFactory implements ExternalSystemSettingsConfigurableFactory<LazarusProjectSettings, LazarusSettings> {
    @Override
    public ProjectSystemId getSystemId() {
        return LazarusConstants.SYSTEM_ID;
    }

    @Override
    public LazarusProjectSettings createProjectSettings() {
        return new LazarusProjectSettings();
    }

    @Override
    public LazarusSettings createSystemSettings() {
        return new LazarusSettings(ProjectManager.getInstance().getDefaultProject());
    }

    @Override
    @RequiredUIAccess
    public AbstractExternalProjectSettingsConfigurable<LazarusProjectSettings> createProjectSettingsConfigurable(LazarusProjectSettings settings,
                                                                                                             ExternalSystemSettingsPlace place) {
        return new LazarusProjectSettingsConfigurable(settings, place);
    }
}
