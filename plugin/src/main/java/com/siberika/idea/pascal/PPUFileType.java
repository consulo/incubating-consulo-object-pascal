package com.siberika.idea.pascal;

import com.intellij.openapi.fileTypes.FileType;
import consulo.ui.image.Image;
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
    public String getDescription() {
        return "Free Pascal compiled unit";
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
