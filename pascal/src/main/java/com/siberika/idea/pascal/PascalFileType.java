package com.siberika.idea.pascal;

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PascalFileType extends LanguageFileType {
    public static final PascalFileType INSTANCE = new PascalFileType();

    public static final List<String> UNIT_EXTENSIONS = Arrays.asList("pas", "pp");
    public static final List<String> PROGRAM_EXTENSIONS = Arrays.asList("dpr", "lpr");

    protected PascalFileType() {
        super(PascalLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getId() {
        return "Pascal";
    }

    @NotNull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("Pascal Source");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "pas";
    }

    @Override
    public Image getIcon() {
        return PascalIcons.UNIT;
    }

}
