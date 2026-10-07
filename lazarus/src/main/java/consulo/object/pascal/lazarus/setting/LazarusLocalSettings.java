package consulo.object.pascal.lazarus.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemLocalSettings;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "LazarusLocalSettings", storages = @Storage(file = StoragePathMacros.WORKSPACE_FILE))
public class LazarusLocalSettings extends AbstractExternalSystemLocalSettings
    implements PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {

    @Inject
    public LazarusLocalSettings(Project project) {
        super(LazarusConstants.SYSTEM_ID, project);
    }

    public static LazarusLocalSettings getInstance(Project project) {
        return project.getInstance(LazarusLocalSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        State state = new State();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(State state) {
        super.loadState(state);
    }
}
