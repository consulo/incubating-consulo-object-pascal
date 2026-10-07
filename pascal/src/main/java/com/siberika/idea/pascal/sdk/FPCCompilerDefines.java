package com.siberika.idea.pascal.sdk;

import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import consulo.content.bundle.Sdk;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
final class FPCCompilerDefines {
    private static final Logger LOG = Logger.getInstance(FPCCompilerDefines.class);

    private static final int TIMEOUT = 30_000;
    private static final String SOURCE_NAME = "defines.pas";
    private static final String SOURCE_TEXT = "begin end.\n";
    private static final Pattern MACRO_DEFINED = Pattern.compile("Macro defined: (\\w+)");
    private static final Pattern MACRO_UNDEFINED = Pattern.compile("Macro undefined: (\\w+)");
    private static final Pattern MACRO_VALUE = Pattern.compile("Macro (\\w+) set to (.*)$");

    private FPCCompilerDefines() {
    }

    static Map<String, Define> read(Sdk sdk) {
        String home = sdk.getHomePath();
        File compiler = home != null ? PascalSdkUtil.getFPCExecutable(home) : null;
        if (compiler == null || !compiler.isFile()) {
            return Collections.emptyMap();
        }
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("fpc-defines");
            Path source = tempDir.resolve(SOURCE_NAME);
            Files.writeString(source, SOURCE_TEXT);
            GeneralCommandLine commandLine = new GeneralCommandLine(compiler.getPath(), "-va", "-s", "-FE" + tempDir, source.toString());
            commandLine.setWorkingDirectory(tempDir);
            ProcessOutput output = CapturingProcessUtil.execAndGetOutput(commandLine, TIMEOUT);
            if (output.isTimeout()) {
                LOG.warn("Timed out reading compiler defines of " + compiler);
                return Collections.emptyMap();
            }
            return parse(output.getStdoutLines());
        }
        catch (IOException | ExecutionException e) {
            LOG.warn("Cannot read compiler defines of " + compiler, e);
            return Collections.emptyMap();
        }
        finally {
            if (tempDir != null) {
                FileUtil.delete(tempDir.toFile());
            }
        }
    }

    static Map<String, Define> parse(List<String> lines) {
        Map<String, Define> result = new LinkedHashMap<>();
        for (String line : lines) {
            Matcher matcher = MACRO_DEFINED.matcher(line);
            if (matcher.find()) {
                String name = matcher.group(1);
                Define previous = result.get(name.toUpperCase(Locale.ROOT));
                result.put(name.toUpperCase(Locale.ROOT), new Define(name, null, -1, previous != null ? previous.value : null));
                continue;
            }
            matcher = MACRO_UNDEFINED.matcher(line);
            if (matcher.find()) {
                result.remove(matcher.group(1).toUpperCase(Locale.ROOT));
                continue;
            }
            matcher = MACRO_VALUE.matcher(line);
            if (matcher.find()) {
                String name = matcher.group(1);
                result.put(name.toUpperCase(Locale.ROOT), new Define(name, null, -1, matcher.group(2).trim()));
            }
        }
        return result;
    }
}
