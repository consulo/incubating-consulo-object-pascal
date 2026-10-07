package consulo.object.pascal.msbuild;

import consulo.annotation.component.ExtensionImpl;
import consulo.msbuild.MSBuildProjectFile;

import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class DelphiProjectFile implements MSBuildProjectFile {
    public static final String EXTENSION = "dproj";

    @Override
    public String getExtension() {
        return EXTENSION;
    }

    @Override
    public Set<String> getCapabilities() {
        return Set.of(DelphiProjectCapability.ID);
    }

    @Override
    public Set<String> getItemTypes() {
        return Set.of(DelphiProjectModel.DELPHI_COMPILE_ITEM, DelphiProjectModel.DCC_REFERENCE_ITEM);
    }

    @Override
    public String getPlatform() {
        return DelphiProjectModel.DEFAULT_PLATFORM;
    }
}
