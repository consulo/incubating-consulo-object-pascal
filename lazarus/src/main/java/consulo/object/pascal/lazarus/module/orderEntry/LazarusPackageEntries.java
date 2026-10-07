package consulo.object.pascal.lazarus.module.orderEntry;

import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public final class LazarusPackageEntries {
    private LazarusPackageEntries() {
    }

    public static void replace(ModifiableRootModel rootModel, List<LazarusPackageOrderEntryModel> models) {
        removeAll(rootModel);
        LazarusPackageOrderEntryType type = LazarusPackageOrderEntryType.getInstance();
        for (LazarusPackageOrderEntryModel model : models) {
            rootModel.addCustomOderEntry(type, model);
        }
    }

    public static void removeAll(ModifiableRootModel rootModel) {
        for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
            if (isPackageEntry(orderEntry)) {
                rootModel.removeOrderEntry(orderEntry);
            }
        }
    }

    public static boolean isPackageEntry(OrderEntry orderEntry) {
        return orderEntry instanceof CustomOrderEntry<?> customOrderEntry && customOrderEntry.getModel() instanceof LazarusPackageOrderEntryModel;
    }
}
