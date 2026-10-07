package com.siberika.idea.pascal;

import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PPUFileType implements FileType {
    public static final PPUFileType INSTANCE = new PPUFileType();

    protected PPUFileType() {
    }

    @NotNull
    @Override
    public String getId() {
        return "FPC_PPU";
    }

    @NotNull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("Free Pascal compiled unit");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "ppu";
    }

    @Override
    public Image getIcon() {
        return PascalIcons.COMPILED;
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
