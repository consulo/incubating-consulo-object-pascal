package com.siberika.idea.pascal.editor.highlighter;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsKeyword(), PascalSyntaxHighlighter.KEYWORDS),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsNumber(), PascalSyntaxHighlighter.NUMBERS),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsString(), PascalSyntaxHighlighter.STRING),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsComment(), PascalSyntaxHighlighter.COMMENT),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsOperator(), PascalSyntaxHighlighter.OPERATORS),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsParentheses(), PascalSyntaxHighlighter.PARENTHESES),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsSymbol(), PascalSyntaxHighlighter.SYMBOLS),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsSemicolon(), PascalSyntaxHighlighter.SEMICOLON),
            new AttributesDescriptor(ObjectPascalLocalize.colorSettingsError(), HighlighterColors.BAD_CHARACTER),
    };

    @Override
    @NotNull
    public LocalizeValue getDisplayName() {
        return ObjectPascalLocalize.colorSettingsName();
    }

    @Override
    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    @Override
    @NotNull
    public SyntaxHighlighter getHighlighter() {
        return new PascalSyntaxHighlighter(null, null);
    }

    @Override
    @NonNls
    @NotNull
    public String getDemoText() {
        return DEMO_TEXT;
    }
}
