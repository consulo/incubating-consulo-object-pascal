package com.siberika.idea.pascal.editor.formatter;

import com.google.common.collect.Iterables;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.util.DocUtil;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * Author: George Bakhtadze
 * Date: 12/07/2016
 */
public class PascalCompleteIdent {

    public static void completeIdent(Editor editor, PascalVariableDeclaration el) {
        /**
         * name => name: _;
         */
        PsiErrorElement error = PsiTreeUtil.findChildOfType(el, PsiErrorElement.class);
        PasTypeDecl typeDecl = el.getTypeDecl();
        if ((null == typeDecl) || (error != null)) {
            int colonPos = PascalSmartEnterProcessor.getChildEndOffset(PasTypes.COLON, el);
            String colonStr = colonPos >= 0 ? "" : ": ";
            String rparenStr = ";";
            PascalNamedElement last = Iterables.getLast(el.getNamedIdentDeclList(), null);
            if (last != null) {
                int offs = colonPos >= 0 ? colonPos + 1 : last.getTextRange().getEndOffset();
                if ((colonPos >= 0) && (typeDecl != null)) {
                    offs = typeDecl.getTextRange().getEndOffset();
                }
                DocUtil.adjustDocument(editor, offs, colonStr + DocUtil.PLACEHOLDER_CARET + rparenStr);
            }
        } else {
            DocUtil.adjustDocument(editor, typeDecl.getTextRange().getEndOffset(), ";" + DocUtil.PLACEHOLDER_CARET);
        }
    }

}
