package consulo.object.pascal.lazarus.setting;

import consulo.externalSystem.setting.DelegatingExternalSystemSettingsListener;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
class LazarusSettingsListenerAdapter extends DelegatingExternalSystemSettingsListener<LazarusProjectSettings> implements LazarusSettingsListener {
    LazarusSettingsListenerAdapter(ExternalSystemSettingsListener<LazarusProjectSettings> delegate) {
        super(delegate);
    }
}
