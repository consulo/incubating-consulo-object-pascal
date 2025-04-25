package com.siberika.idea.pascal.sdk;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.io.StreamUtil;

import java.io.IOException;
import java.io.InputStream;

/**
* Author: George Bakhtadze
* Date: 03/10/2013
*/
public class BuiltinsParser {

    public static final String UNIT_NAME_BUILTINS = "$builtins.pas";
    private static LightVirtualFile BUILTINS = prepareBuiltins();

    private static LightVirtualFile prepareBuiltins() {
        LightVirtualFile res = new LightVirtualFile(UNIT_NAME_BUILTINS, PascalFileType.INSTANCE, "Error occured while preparing builtins");
        InputStream data = BuiltinsParser.class.getResourceAsStream("/builtins.pas");
        try {
            StreamUtil.copyStreamContent(data, res.getOutputStream(null));
        } catch (IOException e) {
        }
        return res;
    }

    public static LightVirtualFile getBuiltinsSource() {
        return BUILTINS;
    }

    public static PascalModule getBuiltinsModule(Project project) {
        return PsiTreeUtil.getChildOfType(PsiManager.getInstance(project).findFile(BUILTINS), PascalModule.class);
    }
}
