package com.siberika.idea.pascal.editor.highlighter;

import com.siberika.idea.pascal.PascalBundle;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.colorScheme.setting.ColorDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
* Author: George Bakhtadze
* Date: 12/5/12
*/
@ExtensionImpl
public class PascalColorsPage implements ColorSettingsPage {
    final static String DEMO_TEXT = "{ File header }\n" +
            "program sample;\n" +
            "var\n" +
            "  i, j: Integer;\n" +
            "  s: string;\n" +
            "  a: array[0..5] of string;\n" +
            "const\n" +
            "  c = 'a';\n" +
            "begin\n" +
            "  @s[1] := 'str';\n" +
            "  i := j mod 200 + j xor j^ + Round(0.7 + .1);\n" +
            "  for i := 0 to length(s)-1 do begin\n" +
            "    writeln('Hello world!');\n" +
            "  end;\n" +
            "  @error?;\n" +
            "end.\n";

    private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
            new AttributesDescriptor(PascalBundle.message("color.settings.keyword"), PascalSyntaxHighlighter.KEYWORDS),
            new AttributesDescriptor(PascalBundle.message("color.settings.number"), PascalSyntaxHighlighter.NUMBERS),
            new AttributesDescriptor(PascalBundle.message("color.settings.string"), PascalSyntaxHighlighter.STRING),
            new AttributesDescriptor(PascalBundle.message("color.settings.comment"), PascalSyntaxHighlighter.COMMENT),
            new AttributesDescriptor(PascalBundle.message("color.settings.operator"), PascalSyntaxHighlighter.OPERATORS),
            new AttributesDescriptor(PascalBundle.message("color.settings.parentheses"), PascalSyntaxHighlighter.PARENTHESES),
            new AttributesDescriptor(PascalBundle.message("color.settings.symbol"), PascalSyntaxHighlighter.SYMBOLS),
            new AttributesDescriptor(PascalBundle.message("color.settings.semicolon"), PascalSyntaxHighlighter.SEMICOLON),
            new AttributesDescriptor(PascalBundle.message("color.settings.error"), HighlighterColors.BAD_CHARACTER),
    };

    @NotNull
    public String getDisplayName() {
        return PascalBundle.message("color.settings.name");
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    @NotNull
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    public SyntaxHighlighter getHighlighter() {
        return new PascalSyntaxHighlighter(null, null);
    }

    @NonNls
    @NotNull
    public String getDemoText() {
        return DEMO_TEXT;
    }

    @Nullable
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

}
