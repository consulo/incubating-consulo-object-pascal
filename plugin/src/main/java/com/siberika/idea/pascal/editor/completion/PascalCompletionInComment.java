package com.siberika.idea.pascal.editor.completion;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.lexer.PascalFlexLexerImpl;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.Define;
import com.siberika.idea.pascal.sdk.Directive;
import com.siberika.idea.pascal.util.DocUtil;
import consulo.content.bundle.Sdk;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.DirectoryIndex;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 04/09/2016
 */
class PascalCompletionInComment {
    private static final Pattern DIRECTIVE = Pattern.compile("\\{\\$?\\w*");
    private static final Pattern DIRECTIVE_PARAM = Pattern.compile("\\{(\\$\\w+)\\s+(\\w*)");
    private static final InsertHandler<LookupElement> INSERT_HANDLER_COMMENT = new CommentInsertHandler();
    private static final InsertHandler<LookupElement> INSERT_HANDLER_COMMENT_CLOSED = new CommentInsertHandlerClosed();

    private static class CommentInsertHandler implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(final InsertionContext context, LookupElement item) {
            DocUtil.adjustDocument(context.getEditor(), context.getEditor().getCaretModel().getOffset(), DocUtil.PLACEHOLDER_CARET);
        }
    }

    private static class CommentInsertHandlerClosed implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(final InsertionContext context, LookupElement item) {
            DocUtil.adjustDocument(context.getEditor(), context.getEditor().getCaretModel().getOffset(), DocUtil.PLACEHOLDER_CARET + "}");
        }
    }

    static void handleComments(CompletionResultSet result, CompletionParameters parameters) {
        PsiElement comment = parameters.getOriginalPosition();
        final boolean needClose = !DocUtil.isSingleLine(parameters.getEditor().getDocument(), comment);
        int ofs = parameters.getOffset() - comment.getTextOffset();
        if (ofs <= 0) {
            return;
        }
        String text = comment.getText().substring(0, ofs);
        if (DIRECTIVE.matcher(text).matches()) {
            for (Map.Entry<String, Directive> entry : retrieveDirectives(comment).entrySet()) {
                result.addElement(withTexts(null, entry.getKey() + (entry.getValue().hasParameters(entry.getKey()) ? " " : ""), entry.getValue().desc)
                        .withInsertHandler(needClose ? INSERT_HANDLER_COMMENT_CLOSED : INSERT_HANDLER_COMMENT));
            }
        } else {
            Matcher m = DIRECTIVE_PARAM.matcher(text);
            if (m.matches()) {
                String id = m.group(1);
                result = result.withPrefixMatcher(m.group(2));
                if (Directive.isDefine(id)) {
                    Map<String, Define> defines = retrieveDefines(comment.getContainingFile() != null ? comment.getContainingFile().getVirtualFile() : null, comment.getProject());
                    for (Define define : defines.values()) {
                        result.addElement(withTexts(define.name + (needClose ? "}" : ""), define.name, getDesc(comment.getProject(), define)));
                    }
                } else {
                    Directive dir = retrieveDirectives(comment).get(id.toUpperCase());
                    if ((dir != null) && (dir.values != null)) {
                        for (String value : dir.values) {
                            result.addElement(withTexts(value + (needClose ? "}" : ""), value, null));
                        }
                    }
                }
            }
        }
    }

    private static LookupElementBuilder withTexts(String lookup, String text, String desc) {
        lookup = lookup != null ? lookup : text;
        return LookupElementBuilder.create(lookup).withLookupString(lookup.toUpperCase()).withLookupString(lookup.toLowerCase())
                .withPresentableText(text).withTypeText(desc, true).withCaseSensitivity(true);
    }

    private static String getDesc(Project project, Define define) {
        if (define.virtualFile != null) {
            VirtualFile root = DirectoryIndex.getInstance(project).getInfoForFile(define.virtualFile).getContentRoot();
            String start = root != null ? root.getPath() : null;
            Document doc = FileDocumentManager.getInstance().getDocument(define.virtualFile);
            String path = define.virtualFile.getPath();
            path = (start != null) && path.startsWith(start) ? root.getPresentableName() + path.substring(start.length()) : path;
            return path + (doc != null ? ":" + doc.getLineNumber(define.offset) : "");
        }
        return PascalBundle.message("completion.defines.source." + (define.offset < 0 ? "builtin" : "commandLine"));
    }

    private static Map<String, Define> retrieveDefines(@Nullable VirtualFile file, @NotNull Project project) {
        PascalFlexLexerImpl lexer = PascalFlexLexerImpl.processFile(project, file);
        return lexer != null ? lexer.getAllDefines() : Collections.<String, Define>emptyMap();
    }

    private static Map<String, Directive> retrieveDirectives(PsiElement comment) {
        Module module = ModuleUtilCore.findModuleForPsiElement(comment);
        Sdk sdk = module != null ? ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class) : null;
        return sdk != null ? BasePascalSdkType.getDirectives(sdk, sdk.getVersionString()) : Collections.<String, Directive>emptyMap();
    }

}
