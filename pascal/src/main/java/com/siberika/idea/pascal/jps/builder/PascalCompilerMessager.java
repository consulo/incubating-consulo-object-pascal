package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import consulo.compiler.CompileContext;
import consulo.localize.LocalizeValue;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Author: George Bakhtadze
 * Date: 20/05/2015
 */
public class PascalCompilerMessager implements CompilerMessager {
    private static final List<String> SUPPRESSED_MSG_ID = Arrays.asList("1018", "10026", "F2063");

    private final CompileContext myContext;
    private Path myWorkingDirectory;

    public PascalCompilerMessager(CompileContext context) {
        myContext = context;
    }

    public void setWorkingDirectory(Path workingDirectory) {
        myWorkingDirectory = workingDirectory;
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
        postMessage(consulo.compiler.CompilerMessageCategory.INFORMATION, msg, path, line, column);
    }

    @Override
    public void info(String msgId, String msg, String path, long line, long column) {
        postMessage(consulo.compiler.CompilerMessageCategory.INFORMATION, msg, path, line, column);
    }

    @Override
    public void warning(String msgId, String msg, String path, long line, long column) {
        postMessage(consulo.compiler.CompilerMessageCategory.WARNING, msg, path, line, column);
    }

    @Override
    public void error(String msgId, String msg, String path, long line, long column) {
        postMessage(consulo.compiler.CompilerMessageCategory.ERROR, msg, path, line, column);
    }

    private void postMessage(consulo.compiler.CompilerMessageCategory category, String msg, String path, long line, long column) {
        Path file = resolve(path);
        String url = file != null ? VirtualFileUtil.pathToUrl(FileUtil.toSystemIndependentName(file.toString())) : null;
        myContext.newMessage(category, LocalizeValue.of(StringUtil.notNullize(msg)))
            .optionalUrl(url)
            .position((int) line, (int) column)
            .add();
    }

    public Path resolve(String path) {
        if (StringUtil.isEmpty(path)) {
            return null;
        }
        try {
            Path file = Path.of(path.trim());
            if (!file.isAbsolute() && myWorkingDirectory != null) {
                file = myWorkingDirectory.resolve(file);
            }
            file = file.normalize();
            return Files.isRegularFile(file) ? file : null;
        }
        catch (InvalidPathException e) {
            return null;
        }
    }
}
