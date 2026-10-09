package com.siberika.idea.pascal.jps.util;

import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.jps.JpsPascalBundle;
import consulo.application.progress.ProgressManager;
import consulo.component.ProcessCanceledException;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.util.CapturingProcessAdapter;
import consulo.process.util.ProcessOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class SysUtils {
    public static final Logger LOG = Logger.getInstance(SysUtils.class.getName());

    public static final int LONG_TIMEOUT = 5 * 60 * 1000;
    public static final int SHORT_TIMEOUT = 5 * 1000;

    private static final long WAIT_INTERVAL_MS = 50;

    @NotNull
    public static ProcessOutput getProcessOutput(final int timeout, @NotNull final String workDir,
                                                 @NotNull final String exePath,
                                                 @NotNull final String... arguments) throws ExecutionException {
        if (!new File(workDir).isDirectory() || !new File(exePath).canExecute()) {
            return new ProcessOutput();
        }

        final GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setWorkDirectory(workDir);
        cmd.setExePath(exePath);
        cmd.addParameters(arguments);

        return execute(cmd, timeout);
    }

    @NotNull
    public static ProcessOutput execute(@NotNull final GeneralCommandLine cmd) throws ExecutionException {
        return execute(cmd, LONG_TIMEOUT);
    }

    @NotNull
    public static ProcessOutput execute(@NotNull final GeneralCommandLine cmd,
                                        final int timeout) throws ExecutionException {
        LOG.info("Executing: " + cmd.getCommandLineString());
        ProcessOutput output = new ProcessOutput();
        ProcessHandler handler = ProcessHandlerBuilder.create(cmd).silentReader().build();
        handler.addProcessListener(new CapturingProcessAdapter(output));
        handler.startNotify();
        long deadline = timeout < 0 ? Long.MAX_VALUE : System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        try {
            while (!handler.waitFor(WAIT_INTERVAL_MS)) {
                ProgressManager.checkCanceled();
                if (System.nanoTime() >= deadline) {
                    handler.destroyProcess();
                    output.setTimeout();
                    return output;
                }
            }
        }
        catch (ProcessCanceledException e) {
            handler.destroyProcess();
            throw e;
        }
        return output;
    }

    @Nullable
    public static String runAndGetStdOut(String workDir, String exePath, int timeoutMs, String...params) throws PascalException {
        final ProcessOutput processOutput;
        try {
            processOutput = getProcessOutput(timeoutMs, workDir, exePath, params);
        } catch (final ExecutionException e) {
            return null;
        }
        int exitCode = processOutput.getExitCode();
        final String stdout = processOutput.getStdout().trim();
        final String stderr = processOutput.getStderr().trim();
        if ((exitCode != 0) && (stdout.isEmpty())) {
            LOG.info(String.format("WARNING: Error running %s. Code: %d", exePath, exitCode));
            LOG.info(String.format("Output: %s", stdout));
            LOG.info(String.format("Error: %s", stderr));
            throw new PascalException(JpsPascalBundle.message("error.exit.code", exePath, exitCode, stderr));
        }
        if (stdout.isEmpty()) {
            return null;
        }
        return stdout;
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            LOG.warn("Error closing resource", e);
        }
    }

    public static File createTempDir(String prefix) {
        try {
            final File file = Files.createTempDirectory(prefix).toFile();
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            LOG.error("Error creating temp dir: " + prefix);
            return null;
        }
    }
}
