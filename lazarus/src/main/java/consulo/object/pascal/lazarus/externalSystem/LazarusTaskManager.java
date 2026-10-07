package consulo.object.pascal.lazarus.externalSystem;

import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.object.pascal.lazarus.LazarusBuildTool;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.lazarus.setting.LazarusExecutionSettings;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputType;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusTaskManager implements ExternalSystemTaskManager<LazarusExecutionSettings> {
    public static final String BUILD_TASK = "build";
    public static final String REBUILD_TASK = "rebuild";
    public static final String BUILD_MODE_TASK_PREFIX = "build ";

    private static final String REBUILD_ALL_FLAG = "-B";

    private final ProcessHandlerBuilderFactory myProcessHandlerBuilderFactory;
    private final Map<ExternalSystemTaskId, ProcessHandler> myProcessHandlers = new ConcurrentHashMap<>();

    public LazarusTaskManager(ProcessHandlerBuilderFactory processHandlerBuilderFactory) {
        myProcessHandlerBuilderFactory = processHandlerBuilderFactory;
    }

    @Override
    public void executeTasks(ExternalSystemTaskId id,
                             List<String> taskNames,
                             String projectPath,
                             @Nullable LazarusExecutionSettings settings,
                             List<String> vmOptions,
                             List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        LazarusBuildTool buildTool = LazarusBuildTool.find();
        if (buildTool == null) {
            throw new ExternalSystemException(LazarusLocalize.taskLazbuildMissing().get());
        }
        Path projectFile = Path.of(settings != null ? settings.getProjectFile() : projectPath);
        String defaultBuildMode = settings != null ? settings.getBuildMode() : null;
        for (String taskName : taskNames) {
            GeneralCommandLine commandLine;
            if (BUILD_TASK.equals(taskName)) {
                commandLine = buildTool.createBuildCommandLine(projectFile, defaultBuildMode);
            }
            else if (REBUILD_TASK.equals(taskName)) {
                commandLine = buildTool.createBuildCommandLine(projectFile, defaultBuildMode);
                commandLine.getParametersList().prependAll(REBUILD_ALL_FLAG);
            }
            else if (taskName.startsWith(BUILD_MODE_TASK_PREFIX)) {
                commandLine = buildTool.createBuildCommandLine(projectFile, taskName.substring(BUILD_MODE_TASK_PREFIX.length()));
            }
            else {
                throw new ExternalSystemException(LazarusLocalize.taskUnknown(taskName).get());
            }
            run(id, commandLine, listener);
        }
    }

    private void run(ExternalSystemTaskId id, GeneralCommandLine commandLine, ExternalSystemTaskNotificationListener listener) {
        listener.onTaskOutput(id, commandLine.getCommandLineString() + "\n", true);
        try {
            ProcessHandler handler = myProcessHandlerBuilderFactory.newBuilder(commandLine).killable().build();
            myProcessHandlers.put(id, handler);
            handler.addProcessListener(new ProcessListener() {
                @Override
                public void onTextAvailable(ProcessEvent event, Key outputType) {
                    listener.onTaskOutput(id, event.getText(), !ProcessOutputType.isStderr(outputType));
                }
            });
            handler.startNotify();
            handler.waitFor();

            Integer exitCode = handler.getExitCode();
            if (exitCode != null && exitCode != 0) {
                throw new ExternalSystemException(LazarusLocalize.taskFailed(exitCode).get());
            }
        }
        catch (ExecutionException e) {
            throw new ExternalSystemException(e.getMessage());
        }
        finally {
            myProcessHandlers.remove(id);
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        ProcessHandler handler = myProcessHandlers.get(id);
        if (handler == null) {
            return false;
        }
        handler.destroyProcess();
        return true;
    }
}
