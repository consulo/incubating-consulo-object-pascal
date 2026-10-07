package consulo.object.pascal.lazarus.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

import java.util.Collection;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@TopicAPI(ComponentScope.PROJECT)
public interface LazarusSettingsListener extends ExternalSystemSettingsListener<LazarusProjectSettings> {
    Class<LazarusSettingsListener> TOPIC = LazarusSettingsListener.class;

    @Override
    default void onProjectRenamed(String oldName, String newName) {
    }

    @Override
    default void onProjectsLoaded(Collection<LazarusProjectSettings> settings) {
    }

    @Override
    default void onProjectsLinked(Collection<LazarusProjectSettings> settings) {
    }

    @Override
    default void onProjectsUnlinked(Set<String> linkedProjectPaths) {
    }

    @Override
    default void onBulkChangeStart() {
    }

    @Override
    default void onBulkChangeEnd() {
    }
}
