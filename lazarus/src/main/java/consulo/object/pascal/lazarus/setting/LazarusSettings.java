package consulo.object.pascal.lazarus.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.externalSystem.autoimport.ExternalSystemProjectId;
import consulo.externalSystem.autoimport.ExternalSystemProjectTracker;
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "LazarusSettings", storages = @Storage("lazarus"))
public class LazarusSettings extends AbstractExternalSystemSettings<LazarusSettings, LazarusProjectSettings, LazarusSettingsListener>
    implements PersistentStateComponent<LazarusSettingsState> {

    @Inject
    public LazarusSettings(Project project) {
        super(LazarusSettingsListener.TOPIC, project);
    }

    public static LazarusSettings getInstance(Project project) {
        return project.getInstance(LazarusSettings.class);
    }

    @Override
    public void subscribe(ExternalSystemSettingsListener<LazarusProjectSettings> listener) {
        getProject().getMessageBus().connect(getProject()).subscribe(LazarusSettingsListener.TOPIC, new LazarusSettingsListenerAdapter(listener));
    }

    @Override
    protected void copyExtraSettingsFrom(LazarusSettings settings) {
    }

    @Override
    protected void checkSettings(LazarusProjectSettings old, LazarusProjectSettings current) {
        Project project = getProject();
        String path = current.getExternalProjectPath();
        if (project.isDefault() || path == null) {
            return;
        }
        if (!Objects.equals(old.getBuildMode(), current.getBuildMode()) || !Objects.equals(old.getSdkName(), current.getSdkName())) {
            ExternalSystemProjectTracker tracker = ExternalSystemProjectTracker.getInstance(project);
            tracker.markDirty(new ExternalSystemProjectId(LazarusConstants.SYSTEM_ID, path));
            tracker.scheduleProjectRefresh();
        }
    }

    @Override
    public LazarusSettingsState getState() {
        LazarusSettingsState state = new LazarusSettingsState();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(LazarusSettingsState state) {
        super.loadState(state);
    }
}
