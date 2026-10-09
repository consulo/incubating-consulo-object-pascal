package consulo.object.pascal.msbuild;

import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.sdk.DelphiSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.msbuild.MSBuildProjectFile;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;

import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class DelphiProjectFile implements MSBuildProjectFile {
    public static final String EXTENSION = "dproj";
    public static final String FPC_PATH_PROPERTY = "FpcPath";
    public static final String BDS_PROPERTY = "BDS";

    private static final Logger LOG = Logger.getInstance(DelphiProjectFile.class);

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

    @RequiredReadAction
    @Override
    public void fillGlobalProperties(Module module, Map<String, String> globalProperties) {
        Sdk sdk = ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class);
        String sdkHome = sdk != null ? sdk.getHomePath() : null;
        if (sdkHome == null) {
            return;
        }
        if (sdk.getSdkType() instanceof DelphiSdkType) {
            globalProperties.put(BDS_PROPERTY, sdkHome);
            return;
        }
        if (!(sdk.getSdkType() instanceof FPCSdkType)) {
            return;
        }
        globalProperties.put(BDS_PROPERTY, "");
        try {
            globalProperties.put(FPC_PATH_PROPERTY, PascalSdkUtil.getFPCExecutable(sdkHome).getPath());
        }
        catch (RuntimeException e) {
            LOG.warn("Free Pascal compiler not found in SDK " + sdk.getName(), e);
        }
    }
}
