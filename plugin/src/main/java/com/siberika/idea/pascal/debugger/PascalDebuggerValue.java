package com.siberika.idea.pascal.debugger;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.debugger.gdb.GdbVariableObject;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.util.DocUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.ApplicationManager;
import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.document.Document;
import consulo.execution.debug.XDebuggerUtil;
import consulo.execution.debug.XSourcePosition;
import consulo.execution.debug.frame.*;
import consulo.execution.debug.frame.presentation.XErrorValuePresentation;
import consulo.execution.debug.frame.presentation.XValuePresentation;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class PascalDebuggerValue extends XValue {

    private GdbVariableObject variableObject;

    public PascalDebuggerValue(GdbVariableObject variableObject) {
        this.variableObject = variableObject;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        if (variableObject.getError() != null) {
            node.setPresentation(null, new XErrorValuePresentation(variableObject.getError()), hasChildren());
            return;
        }
        Image icon = PascalIcons.VARIABLE;
        switch (variableObject.getFieldType()) {
            case ROUTINE: {
                icon = PascalIcons.ROUTINE;
                break;
            }
            case CONSTANT: {
                icon = PascalIcons.CONSTANT;
                break;
            }
            case PROPERTY: {
                icon = PascalIcons.PROPERTY;
                break;
            }
            case PSEUDO_VARIABLE: {
                icon = PascalIcons.COMPILED;
                break;
            }
        }
        node.setPresentation(icon, new XValuePresentation() {

            @NotNull
            @Override
            public String getSeparator() {
                return ": ";
            }

            @Override
            public void renderValue(@NotNull XValueTextRenderer renderer) {
                if (variableObject.getType() != null) {
                    renderer.renderComment(variableObject.getType());
                    renderer.renderSpecialSymbol(" = ");
                }

                if (variableObject.getAdditional() != null) {
                    renderer.renderComment("(" + variableObject.getAdditional() + ") ");
                }
                String value = variableObject.getPresentation();
                if ((value != null) && (value.startsWith("'") || value.startsWith("#"))) {
                    renderer.renderValue(value, DefaultLanguageHighlighterColors.STRING);
                } else {
                    renderer.renderValue(value != null ? value : "??");
                }
            }

        }, hasChildren());
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (variableObject.getFrame().getProcess().backend.getData().getBoolean(PascalSdkData.Keys.DEBUGGER_RETRIEVE_CHILDS)) {
            variableObject.getFrame().getProcess().getVariableManager().computeValueChildren(variableObject.getKey(), node);
        } else {
            node.setErrorMessage(PascalBundle.message("debug.error.subfields.disabled"));
        }
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        XSourcePosition sp = variableObject.getFrame().getSourcePosition();
        PsiFile file = sp != null ? getPsiFile(sp, variableObject.getFrame().getProcess().getProject()) : null;
        Document doc = file != null ? PsiDocumentManager.getInstance(variableObject.getFrame().getProcess().getProject()).getDocument(file) : null;
        if (doc != null) {
            int lineNum = sp.getLine() - 1;
            final Integer startLine = variableObject.getFrame().getBlockInfo().getStartLine();
            while (lineNum >= startLine) {
                String line = DocUtil.getWholeLine(doc, lineNum);
                Pattern pattern = Pattern.compile("(?i)\\b" + variableObject.getName() + "\\b");
                if (pattern.matcher(line).find()) {
                    navigatable.setSourcePosition(XDebuggerUtil.getInstance().createPosition(file.getVirtualFile(), lineNum));
                    return;
                }
                lineNum--;
            }
        }
    }

    @Nullable
    @RequiredReadAction
    public static PsiFile getPsiFile(@Nullable XSourcePosition position, Project project) {
        ApplicationManager.getApplication().assertReadAccessAllowed();
        if (position != null) {
            VirtualFile file = position.getFile();
            if (file.isValid()) {
                return PsiManager.getInstance(project).findFile(file);
            }
        }
        return null;
    }

    private boolean hasChildren() {
        return (variableObject.getChildrenCount() != null) && (variableObject.getChildrenCount() > 0);
    }
}
