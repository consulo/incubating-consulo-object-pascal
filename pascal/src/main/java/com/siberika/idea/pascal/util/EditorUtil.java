package com.siberika.idea.pascal.util;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.EditorActionHandler;
import consulo.codeEditor.action.EditorActionManager;
import consulo.dataContext.DataContext;
import consulo.dataContext.DataManager;
import consulo.document.Document;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.hint.HintManager;
import consulo.language.editor.ui.awt.HintUtil;
import consulo.language.editor.ui.navigation.PsiTargetNavigationService;
import consulo.language.editor.ui.navigation.PsiTargetPresentationFactory;
import consulo.language.editor.ui.navigation.TargetPresentationProvider;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.event.ComponentEvent;
import consulo.ui.ex.RelativePoint;
import consulo.ui.ex.action.IdeActions;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import jakarta.annotation.Nonnull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * @author George Bakhtadze
 * @since 2015-07-21
 */
public class EditorUtil {
    public static final int NO_ITEMS_HINT_TIMEOUT_MS = 2000;

    public static <T extends PsiElement> void navigateTo(Editor editor, @Nonnull LocalizeValue title, Collection<T> targets) {
        Application.get().getInstance(PsiTargetNavigationService.class)
            .newNavigator(() -> targets)
            .presentationProvider(PASCAL_PRESENTATION)
            .title(title)
            .navigate(editor, editor.getProject());
    }

    public static <T extends PsiElement> void navigateTo(
        ComponentEvent<?> event,
        @Nonnull LocalizeValue title,
        @Nonnull LocalizeValue emptyTitle,
        Project project,
        Collection<T> targets
    ) {
        Application.get().getInstance(PsiTargetNavigationService.class)
            .newNavigator(() -> targets)
            .presentationProvider(PASCAL_PRESENTATION)
            .title(title)
            .emptyText(emptyTitle)
            .navigate(event, project);
    }

    public static void showErrorHint(@Nonnull LocalizeValue title, RelativePoint relativePoint) {
        final JLabel label = new JLabel(title.get());
        label.setBorder(HintUtil.createHintBorder());
        label.setBackground(TargetAWT.to(HintUtil.getErrorColor()));
        label.setOpaque(true);
        HintManager.getInstance().showHint(label, relativePoint, 0, NO_ITEMS_HINT_TIMEOUT_MS);
    }

    public static void showInformationHint(Editor editor, @Nonnull LocalizeValue message) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                HintManager.getInstance().showInformationHint(editor, message);
            }
        });
    }

    public static RelativePoint getHintPos(Editor editor) {
        return new RelativePoint(editor.getComponent(), new Point(0, 0));
    }

    public static void moveToLineEnd(Editor editor) {
        EditorActionHandler actionHandler = EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_END);
        final DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
        if (dataContext != null) {
            actionHandler.execute(editor, editor.getCaretModel().getCurrentCaret(), dataContext);
        }
    }

    public static final TargetPresentationProvider<PsiElement> PASCAL_PRESENTATION = element ->
        Application.get().getInstance(PsiTargetPresentationFactory.class)
            .presentationBuilder(element)
            .withPresentableText(LocalizeValue.of(getElementText(element)))
            .withContainerText(LocalizeValue.empty())
            .withLocationText(LocalizeValue.of(getRightText(element)))
            .build();

    @RequiredReadAction
    private static String getElementText(PsiElement element) {
        if (!element.isValid()) {
            return "<invalid>";
        }
        if (element instanceof PascalNamedElement namedElement) {
            StringBuilder sb = new StringBuilder();
            if (element instanceof PasEntityScope scope) {
                PasEntityScope owner = scope.getContainingScope();
                if (owner != null) {
                    sb.append(owner.getName()).append(".");
                }
            }
            sb.append(ResolveUtil.cleanupName(PsiUtil.getFieldName(namedElement)));
            return sb.toString();
        }
        return element.getText();
    }

    @RequiredReadAction
    private static String getRightText(PsiElement element) {
        if (!element.isValid()) {
            return "<invalid>";
        }
        PsiFile file = element.getContainingFile();
        if (file != null) {
            String line;
            if (!PsiUtil.isFromLibrary(element)) {
                Document doc = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
                line = (doc != null) ? String.valueOf(doc.getLineNumber(element.getTextOffset()) + 1) : "-";
            }
            else {
                line = "-";
            }
            return String.format("%s (%s)", file.getName(), line);
        }
        return "-";
    }

    public static Editor getEditor(Project project) {
        return FileEditorManager.getInstance(project).getSelectedTextEditor();
    }
}
