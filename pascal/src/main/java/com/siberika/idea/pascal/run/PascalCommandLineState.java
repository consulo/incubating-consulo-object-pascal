package com.siberika.idea.pascal.run;

import consulo.execution.configuration.CommandLineState;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.console.TextConsoleBuilderFactory;
import consulo.process.ExecutionException;
import consulo.process.ProcessConsoleType;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import org.jetbrains.annotations.NotNull;

public class PascalCommandLineState extends CommandLineState {

    private final PascalRunConfiguration runConfiguration;
    private final boolean fixIOBuffering;

    public PascalCommandLineState(PascalRunConfiguration runConfiguration, ExecutionEnvironment env, boolean fixIOBuffering) {
        super(env);
        this.runConfiguration = runConfiguration;
        this.fixIOBuffering = fixIOBuffering;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        ProcessHandler handler = runConfiguration.getProject().getApplication().getInstance(ProcessHandlerBuilderFactory.class)
            .newBuilder(runConfiguration.createCommandLine(getEnvironment()))
            .killable()
            .colored()
            .consoleType(fixIOBuffering ? ProcessConsoleType.EXTERNAL_EMULATION : ProcessConsoleType.BUILTIN)
            .build();
        setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(runConfiguration.getProject()));
        return handler;
    }

}
