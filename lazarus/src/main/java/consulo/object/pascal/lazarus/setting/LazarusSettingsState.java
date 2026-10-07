package consulo.object.pascal.lazarus.setting;

import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.util.xml.serializer.annotation.AbstractCollection;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusSettingsState implements AbstractExternalSystemSettings.State<LazarusProjectSettings> {
    private Set<LazarusProjectSettings> myProjectSettings = new TreeSet<>();

    @Override
    @AbstractCollection(surroundWithTag = false, elementTypes = {LazarusProjectSettings.class})
    public Set<LazarusProjectSettings> getLinkedExternalProjectsSettings() {
        return myProjectSettings;
    }

    @Override
    public void setLinkedExternalProjectsSettings(Set<LazarusProjectSettings> settings) {
        if (settings != null) {
            myProjectSettings.addAll(settings);
        }
    }
}
