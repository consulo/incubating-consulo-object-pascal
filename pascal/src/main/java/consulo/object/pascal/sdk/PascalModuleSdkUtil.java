package consulo.object.pascal.sdk;

import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.content.bundle.SdkTypeId;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.ModuleExtensionWithSdkOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.module.extension.ModuleExtensionWithSdk;
import jakarta.annotation.Nullable;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public final class PascalModuleSdkUtil {
    private PascalModuleSdkUtil() {
    }

    @Nullable
    public static Sdk findDefaultSdk(SdkTypeId... types) {
        SdkTable sdkTable = SdkTable.getInstance();
        Sdk withHome = null;
        Sdk fallback = null;
        for (SdkTypeId type : types) {
            for (Sdk sdk : sdkTable.getSdksOfType(type)) {
                if (hasHome(sdk)) {
                    if (BasePascalSdkType.isConfigured(sdk)) {
                        return sdk;
                    }
                    if (withHome == null) {
                        withHome = sdk;
                    }
                }
                if (fallback == null) {
                    fallback = sdk;
                }
            }
        }
        return withHome != null ? withHome : fallback;
    }

    public static void ensureSdkEntry(ModifiableRootModel rootModel, ModuleExtensionWithSdk<?> extension) {
        for (OrderEntry entry : rootModel.getOrderEntries()) {
            if (entry instanceof ModuleExtensionWithSdkOrderEntry sdkEntry && extension.getId().equals(sdkEntry.getModuleExtensionId())) {
                return;
            }
        }
        rootModel.addModuleExtensionSdkEntry(extension);
    }

    private static boolean hasHome(Sdk sdk) {
        String home = sdk.getHomePath();
        if (home == null) {
            return false;
        }
        try {
            return Files.isDirectory(Path.of(home));
        }
        catch (InvalidPathException e) {
            return false;
        }
    }
}
