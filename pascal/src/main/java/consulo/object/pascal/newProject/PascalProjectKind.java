package consulo.object.pascal.newProject;

import consulo.localize.LocalizeValue;
import consulo.object.pascal.localize.ObjectPascalLocalize;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public enum PascalProjectKind {
    APPLICATION("application"),
    LIBRARY("library");

    private final String myTemplateName;

    PascalProjectKind(String templateName) {
        myTemplateName = templateName;
    }

    public String getTemplateName() {
        return myTemplateName;
    }

    public LocalizeValue getDisplayName() {
        return this == APPLICATION ? ObjectPascalLocalize.newProjectKindApplication() : ObjectPascalLocalize.newProjectKindLibrary();
    }
}
