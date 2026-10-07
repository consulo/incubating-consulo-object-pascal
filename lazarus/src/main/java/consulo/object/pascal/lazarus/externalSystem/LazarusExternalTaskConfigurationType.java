package consulo.object.pascal.lazarus.externalSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusExternalTaskConfigurationType extends AbstractExternalSystemTaskConfigurationType {
    public LazarusExternalTaskConfigurationType() {
        super(LazarusConstants.SYSTEM_ID);
    }
}
