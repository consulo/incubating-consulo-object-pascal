package consulo.object.pascal.run;

import com.siberika.idea.pascal.run.PascalRunConfiguration;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.FileDocumentManager;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.nativeDev.profiler.NativeProfilerLauncher;
import consulo.process.ExecutionException;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class PascalProfilerRunner extends DefaultProgramRunner {
    @Override
    public String getRunnerId() {
        return "PascalProfilerRunner";
    }

    @Override
    public boolean canRun(String executorId, RunProfile profile) {
        return profile instanceof PascalRunConfiguration && NativeProfilerLauncher.canRun(executorId, profile);
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(RunProfileState state, ExecutionEnvironment environment) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();
        PascalRunConfiguration configuration = (PascalRunConfiguration) environment.getRunProfile();
        return NativeProfilerLauncher.execute(environment, configuration.createCommandLine(environment));
    }
}
