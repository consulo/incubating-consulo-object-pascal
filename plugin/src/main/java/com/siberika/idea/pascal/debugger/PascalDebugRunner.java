package com.siberika.idea.pascal.debugger;

import com.siberika.idea.pascal.run.PascalRunConfiguration;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.*;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.GenericProgramRunner;
import consulo.execution.ui.RunContentDescriptor;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.process.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class PascalDebugRunner extends GenericProgramRunner {
    @NotNull
    public String getRunnerId() {
        return "com.siberika.idea.pascal.run.PascalDebugRunner";
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state,
                                             @NotNull final ExecutionEnvironment environment) throws ExecutionException {
        XDebuggerManager xDebuggerManager = XDebuggerManager.getInstance(environment.getProject());

        final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);

        final PascalRunConfiguration conf = (PascalRunConfiguration) environment.getRunProfile();

        return xDebuggerManager.startSession(environment, new XDebugProcessStarter() {
            @NotNull
            @Override
            public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                return PascalDebugFactory.createXDebugProcess(session, environment, executionResult);
            }
        }).getRunContentDescriptor();
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (profile instanceof PascalRunConfiguration) {
            Module module = ((PascalRunConfiguration) profile).getConfigurationModule().getModule();
            Sdk sdk = module != null ? ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class) : null;
            return ((null == sdk) || (sdk.getSdkType() instanceof FPCSdkType)) &&
                    executorId.equals(DefaultDebugExecutor.EXECUTOR_ID);
        } else {
            return false;
        }

    }

}
