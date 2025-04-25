package com.siberika.idea.pascal;

import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: George Bakhtadze
 * Date: 21.05.2015
 */
public class DCUFileType implements FileType {
    public static final DCUFileType INSTANCE = new DCUFileType();

    protected DCUFileType() {
    }

    @NotNull
    @Override
    public String getName() {
        return "DELPHI_DCU";
    }

    @NotNull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("Delphi compiled unit");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "dcu";
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

    @Nullable
    @Override
    public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
        return null;
    }

}
