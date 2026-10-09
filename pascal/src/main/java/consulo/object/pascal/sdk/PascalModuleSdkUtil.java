package consulo.object.pascal.sdk;

import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.content.base.BinariesOrderRootType;
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
    private static final String BUILTINS_FILE = "/builtins.pas";

    private PascalModuleSdkUtil() {
    }

    @Nullable
    public static Sdk findDefaultSdk(SdkTypeId... types) {
        SdkTable sdkTable = SdkTable.getInstance();
        Sdk best = null;
        int bestRank = -1;
        for (SdkTypeId type : types) {
            for (Sdk sdk : sdkTable.getSdksOfType(type)) {
                int rank = rank(sdk);
                if (rank > bestRank) {
                    best = sdk;
                    bestRank = rank;
                }
            }
        }
        return best;
    }

    public static void ensureSdkEntry(ModifiableRootModel rootModel, ModuleExtensionWithSdk<?> extension) {
        for (OrderEntry entry : rootModel.getOrderEntries()) {
            if (entry instanceof ModuleExtensionWithSdkOrderEntry sdkEntry && extension.getId().equals(sdkEntry.getModuleExtensionId())) {
                return;
            }
        }
        rootModel.addModuleExtensionSdkEntry(extension);
    }

    private static int rank(Sdk sdk) {
        if (!hasHome(sdk)) {
            return 0;
        }
        int rank = 1;
        if (BasePascalSdkType.isConfigured(sdk)) {
            rank += 2;
        }
        if (hasBuiltins(sdk)) {
            rank += 1;
        }
        return rank;
    }

    private static boolean hasBuiltins(Sdk sdk) {
        for (String url : sdk.getRootProvider().getUrls(BinariesOrderRootType.ID)) {
            if (url.endsWith(BUILTINS_FILE)) {
                return true;
            }
        }
        return false;
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
