package com.siberika.idea.pascal.editor.refactoring;

import com.siberika.idea.pascal.util.StrUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.refactoring.move.MoveCallback;
import consulo.language.editor.refactoring.move.MoveHandlerDelegate;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;

@ExtensionImpl
public class PascalMoveHandler extends MoveHandlerDelegate {

    @Override
    public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer) {
        System.out.println(String.format("canMove: [%s] => %s", Arrays.toString(elements), StrUtil.toDebugString(targetContainer)));
        return super.canMove(elements, targetContainer);
    }

    @Override
    public boolean canMove(DataContext dataContext) {
        System.out.println("canMode()");
        return super.canMove(dataContext);
    }

    @Override
    public boolean isValidTarget(@Nullable PsiElement targetElement, PsiElement[] sources) {
        System.out.println(String.format("isValidTarget: [%s] <= %s", StrUtil.toDebugString(targetElement), Arrays.toString(sources)));
        return super.isValidTarget(targetElement, sources);
    }

    @Override
    public void doMove(Project project, PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable MoveCallback callback) {
        System.out.println("doMove");
        super.doMove(project, elements, targetContainer, callback);
    }

    @Override
    public PsiElement adjustTargetForMove(DataContext dataContext, PsiElement targetContainer) {
        System.out.println("adjustTargetForMove");
        return super.adjustTargetForMove(dataContext, targetContainer);
    }

    @Nullable
    @Override
    public PsiElement[] adjustForMove(Project project, PsiElement[] sourceElements, PsiElement targetElement) {
        System.out.println("adjustForMove");
        return super.adjustForMove(project, sourceElements, targetElement);
    }

    @Override
    public boolean tryToMove(PsiElement element, Project project, DataContext dataContext, @Nullable PsiReference reference, Editor editor) {
        System.out.println(String.format("tryToMove: %s, ref: %s", StrUtil.toDebugString(element), reference != null ? StrUtil.toDebugString(reference.getElement()) : "<null>"));
        return super.tryToMove(element, project, dataContext, reference, editor);
    }

    @Override
    public void collectFilesOrDirsFromContext(DataContext dataContext, Set<PsiElement> filesOrDirs) {
        System.out.println("collectFilesOrDirsFromContext: " + Arrays.toString(filesOrDirs.toArray()));
        super.collectFilesOrDirsFromContext(dataContext, filesOrDirs);
    }

    @Override
    public boolean isMoveRedundant(PsiElement source, PsiElement target) {
        System.out.println("isMoveRedundant");
        return super.isMoveRedundant(source, target);
    }
}
