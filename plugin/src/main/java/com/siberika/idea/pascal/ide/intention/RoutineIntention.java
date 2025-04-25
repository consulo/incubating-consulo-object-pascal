package com.siberika.idea.pascal.ide.intention;

import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import consulo.codeEditor.Editor;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.intention.BaseElementAtCaretIntentionAction;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.lang.Pair;
import org.jetbrains.annotations.Nullable;

abstract class RoutineIntention extends BaseElementAtCaretIntentionAction {

    private static final TokenSet TOKENS_ATTRIBUTE = TokenSet.create(PasTypes.COMMA, PasTypes.LBRACK, PasTypes.RBRACK, PasTypes.LPAREN, PasTypes.RPAREN);

    PascalRoutine getTargetRoutine(Editor editor, PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        PsiElement target = SectionToggle.getRoutineTarget(routine);
        return target instanceof PascalRoutine ? (PascalRoutine) target : null;
    }

    PascalRoutine getRoutineHeader(Editor editor, PsiElement element) {
        PascalRoutine routine = PsiTreeUtil.getParentOfType(element, PascalRoutine.class);
        if (routine instanceof PascalExportedRoutine) {
            return routine;
        } else if (routine instanceof PasRoutineImplDecl) {
            PasProcBodyBlock block = ((PasRoutineImplDecl) routine).getProcBodyBlock();
            Integer blockOffs = block != null ? block.getTextOffset() : null;
            return (null == blockOffs) || (editor.getCaretModel().getOffset() < blockOffs) ? routine : null;
        } else {
            return null;
        }
    }

    void changeToProcedure(Project project, PascalRoutine routine) {
        clearReturnType(routine);
        switchKeyword(project, routine, PasTypes.FUNCTION, "procedure");
    }

    void switchKeyword(Project project, PascalRoutine routine, IElementType keywordType, String newKeyword) {
        ASTNode routineKey = routine.getNode().findChildByType(keywordType);
        if (routineKey != null) {
            routine.getNode().replaceChild(routineKey, PasElementFactory.createLeafFromText(project, newKeyword).getNode());
        }
    }

    void clearReturnType(PascalRoutine routine) {
        Pair<PsiElement, PsiElement> returnType = findReturnType(routine);
        if (returnType != null) {
            routine.deleteChildRange(returnType.first, returnType.second);
        }
    }

    Pair<PsiElement, PsiElement> findReturnType(@Nullable PascalRoutine routine) {
        ASTNode typeStartNode = routine != null ? routine.getNode().findChildByType(PasTypes.COLON) : null;
        if (typeStartNode != null) {
            PsiElement typeStart = typeStartNode.getPsi();
            PsiElement typeEnd = typeStart;
            do {
                typeEnd = typeEnd.getNextSibling();
                if (typeEnd instanceof PasTypeDecl) {
                    typeEnd = typeEnd.getNextSibling();
                    break;
                }
            } while (belongsToReturnType(typeEnd));
            return typeStart != typeEnd.getPrevSibling() ? Pair.create(typeStart, typeEnd.getPrevSibling()) : null;
        }
        return null;
    }

    private boolean belongsToReturnType(PsiElement element) {
        if (null == element) {
            return false;
        }
        if (PsiTreeUtil.instanceOf(element, PasTypeDecl.class, PasCustomAttributeDecl.class, PsiComment.class)) {
            return true;
        }
        if (element instanceof PsiWhiteSpace) {
            return !element.textContains('\n');
        }
        if (element instanceof PsiErrorElement) {
            return !";".equals(element.getText());
        }
        return TOKENS_ATTRIBUTE.contains(element.getNode().getElementType());
    }

}
