package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import consulo.compiler.CompileContext;
import consulo.logging.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Author: George Bakhtadze
 * Date: 20/05/2015
 */
public class PascalCompilerMessager implements CompilerMessager {
    private static final Logger LOG = Logger.getInstance(PascalCompilerMessager.class);

    private static final List<String> SUPPRESSED_MSG_ID = Arrays.asList("1018", "10026", "F2063");

    private final CompileContext context;

    public PascalCompilerMessager(CompileContext context) {
        this.context = context;
    }

    static void createMessage(CompilerMessageCategory category, String line, Matcher matcher, CompilerMessager messager) {
        int lineNum = -1;
        int colNum = -1;
        String msgId = null;
        String message = null;
        String url = "";
        if (null != matcher) {
            int groupCount = matcher.groupCount();
            if (groupCount >= 5) {
                url = matcher.group(2);
                try {
                    lineNum = Integer.parseInt(matcher.group(3));
                    colNum = Integer.parseInt(matcher.group(5));
                } catch (NumberFormatException ignore) {}
            }
            msgId = matcher.group(groupCount - 1);
            message = matcher.group(groupCount);
        }
        message = message != null ? message : line;
        if (isErrorSuppressNeeded(msgId, message)) {
            category = CompilerMessageCategory.WARNING;
        }

        if (CompilerMessageCategory.ERROR.equals(category)) {
            messager.error(msgId, message, url, lineNum, colNum);
        } else if (CompilerMessageCategory.WARNING.equals(category)) {
            messager.warning(msgId, message, url, lineNum, colNum);
        } else if (CompilerMessageCategory.HINT.equals(category)) {
            messager.hint(msgId, message, url, lineNum, colNum);
        } else {
            messager.info(msgId, message, url, lineNum, colNum);
        }
    }

    private static boolean isErrorSuppressNeeded(String msgId, String message) {
        return SUPPRESSED_MSG_ID.contains(msgId) || message.endsWith("returned an error exitcode");
    }

    @Override
    public void hint(String msgId, String msg, String path, long line, long column) {
        context.addMessage(consulo.compiler.CompilerMessageCategory.INFORMATION, msg, path, (int)line, (int)column);
    }

    @Override
    public void info(String msgId, String msg, String path, long line, long column) {
        context.addMessage(consulo.compiler.CompilerMessageCategory.INFORMATION, msg, path, (int) line, (int) column);
    }

    @Override
    public void warning(String msgId, String msg, String path, long line, long column) {
        context.addMessage(consulo.compiler.CompilerMessageCategory.WARNING, msg, path, (int) line, (int) column);
    }

    @Override
    public void error(String msgId, String msg, String path, long line, long column) {
        context.addMessage(consulo.compiler.CompilerMessageCategory.ERROR, msg, path, (int) line, (int) column);
    }
}
