package consulo.object.pascal.lazarus;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import consulo.xml.language.XmlFileType;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(XmlFileType.INSTANCE, "lpi;lpk;lps");
    }
}
