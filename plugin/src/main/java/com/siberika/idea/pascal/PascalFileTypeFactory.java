package com.siberika.idea.pascal;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
@ExtensionImpl
public class PascalFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
      fileTypeConsumer.consume(PascalFileType.INSTANCE, "pas;pp;lpr;dpr;inc");
      fileTypeConsumer.consume(PPUFileType.INSTANCE, "ppu");
      fileTypeConsumer.consume(DCUFileType.INSTANCE, "dcu");

      // TODO !
//      FileType xmlFileType = fileTypeConsumer.getStandardFileTypeByName("XML");
//      if(xmlFileType != null) {
//          fileTypeConsumer.consume(xmlFileType, "lpi;dproj");
//      }
  }
}
