package consulo.object.pascal.lazarus.setting;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ProjectConfigurable;
import consulo.configurable.StandardConfigurableIds;
import consulo.externalSystem.service.setting.AbstractExternalSystemConfigurable;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusConfigurable extends AbstractExternalSystemConfigurable<LazarusProjectSettings, LazarusSettingsListener, LazarusSettings>
    implements ProjectConfigurable {
    @Inject
    public LazarusConfigurable(Project project) {
        super(project, LazarusConstants.SYSTEM_ID);
    }

    @Override
    public String getId() {
        return "execution.lazarus";
    }

    @Nullable
    @Override
    public String getParentId() {
        return StandardConfigurableIds.EXECUTION_GROUP;
    }
}
