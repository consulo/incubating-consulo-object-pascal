package com.siberika.idea.pascal.editor.completion;

import com.siberika.idea.pascal.util.DocUtil;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiElement;

public class CaretAdjustInsertHandler implements InsertHandler<LookupElement> {

    private final int offset;

    public CaretAdjustInsertHandler(int offset) {
        this.offset = offset;
    }

    @Override
    public void handleInsert(final InsertionContext context, LookupElement item) {
        final PsiElement ending = context.getFile().findElementAt(context.getTailOffset());
        if (!(ending instanceof LeafPsiElement) || !";".equals(ending.getText())) {
            DocUtil.adjustDocument(context.getDocument(), context.getTailOffset(), ";");
        }
        context.getEditor().getCaretModel().moveToOffset(offset);
    }

}
