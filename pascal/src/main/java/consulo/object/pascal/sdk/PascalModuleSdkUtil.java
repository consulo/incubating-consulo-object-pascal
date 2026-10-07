package consulo.object.pascal.sdk;

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
import java.util.List;

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
        Sdk fallback = null;
        for (SdkTypeId type : types) {
            List<Sdk> sdks = sdkTable.getSdksOfType(type);
            for (Sdk sdk : sdks) {
                if (hasHome(sdk)) {
                    return sdk;
                }
            }
            if (fallback == null && !sdks.isEmpty()) {
                fallback = sdks.get(0);
            }
        }
        return fallback;
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
