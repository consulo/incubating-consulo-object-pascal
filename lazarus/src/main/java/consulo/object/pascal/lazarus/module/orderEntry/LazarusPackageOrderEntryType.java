package consulo.object.pascal.lazarus.module.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryTypeProvider;
import consulo.util.xml.serializer.InvalidDataException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusPackageOrderEntryType implements CustomOrderEntryTypeProvider<LazarusPackageOrderEntryModel> {
    public static final String ID = "lazarus-package";

    private static final String NAME_ATTRIBUTE = "name";
    private static final String FILE_ATTRIBUTE = "file";
    private static final String SOURCE_ROOTS_ELEMENT = "sourceRoots";
    private static final String EXCLUDED_ROOTS_ELEMENT = "excludedRoots";
    private static final String ROOT_ELEMENT = "root";
    private static final String URL_ATTRIBUTE = "url";

    public static LazarusPackageOrderEntryType getInstance() {
        return EP.findExtensionOrFail(Application.get(), LazarusPackageOrderEntryType.class);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LazarusPackageOrderEntryModel loadOrderEntry(Element element, ModuleRootLayer moduleRootLayer) throws InvalidDataException {
        String name = element.getAttributeValue(NAME_ATTRIBUTE);
        String fileUrl = element.getAttributeValue(FILE_ATTRIBUTE);
        if (name == null || fileUrl == null) {
            throw new InvalidDataException("Lazarus package order entry without a name or a package file");
        }
        return new LazarusPackageOrderEntryModel(name, fileUrl, readRoots(element, SOURCE_ROOTS_ELEMENT), readRoots(element, EXCLUDED_ROOTS_ELEMENT));
    }

    @Override
    public void storeOrderEntry(Element element, LazarusPackageOrderEntryModel model) {
        element.setAttribute(NAME_ATTRIBUTE, model.getName());
        element.setAttribute(FILE_ATTRIBUTE, model.getPackageFileUrl());
        writeRoots(element, SOURCE_ROOTS_ELEMENT, model.getSourceRootUrls());
        writeRoots(element, EXCLUDED_ROOTS_ELEMENT, model.getExcludedRootUrls());
    }

    private static List<String> readRoots(Element element, String containerName) {
        List<String> result = new ArrayList<>();
        Element container = element.getChild(containerName);
        if (container == null) {
            return result;
        }
        for (Element root : container.getChildren(ROOT_ELEMENT)) {
            String url = root.getAttributeValue(URL_ATTRIBUTE);
            if (url != null) {
                result.add(url);
            }
        }
        return result;
    }

    private static void writeRoots(Element element, String containerName, List<String> urls) {
        if (urls.isEmpty()) {
            return;
        }
        Element container = new Element(containerName);
        for (String url : urls) {
            container.addContent(new Element(ROOT_ELEMENT).setAttribute(URL_ATTRIBUTE, url));
        }
        element.addContent(container);
    }
}
