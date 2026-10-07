package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.module.PascalModuleType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.application.ReadAction;
import consulo.content.bundle.Sdk;
import consulo.execution.configuration.*;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.nativeDev.debugger.NativeDebuggableRunProfile;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.profiler.NativeProfilableRunProfile;
import consulo.object.pascal.debugger.PascalDebuggerSetup;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.object.pascal.module.extension.PascalBuildModuleExtension;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.cmd.ParametersListUtil;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 06/01/2013
 */
public class PascalRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
        implements PascalRunConfigurationParams, RunConfigurationWithSuppressedDefaultRunAction, NativeDebuggableRunProfile,
        NativeProfilableRunProfile {

    private static final String ATTR_PROGRAM_FILE_NAME = "program_file_name";
    private static final String ATTR_PROGRAM_PARAMETERS = "program_parameters";
    private static final String ATTR_WORKING_DIRECTORY = "working_directory";
    private static final String ATTR_PASS_PARENT_ENVS = "pass_parent_envs";
    private static final String ELEMENT_ENV = "env";
    private static final String ATTR_ENV_NAME = "name";
    private static final String ATTR_ENV_VALUE = "value";

    private String parameters;
    private String workingDirectory;
    private String programFileName;
    private Map<String, String> envs = new LinkedHashMap<>();
    private boolean passParentEnvs = true;
    private boolean fixIOBuffering = true;
    private boolean debugMode = false;

    public PascalRunConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
    }

    @Override
    public Collection<Module> getValidModules() {
        return getAllModules();
    }

    @NotNull
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new PascalRunConfigurationEditor(getProject());
    }

    Module findModule(@NotNull ExecutionEnvironment env) {
        Module result = null;
        if ((env.getRunnerAndConfigurationSettings() != null) &&
            (env.getRunnerAndConfigurationSettings().getConfiguration() instanceof PascalRunConfiguration)) {
            PascalRunConfiguration configuration = (PascalRunConfiguration) env.getRunnerAndConfigurationSettings().getConfiguration();
            result = configuration.getConfigurationModule().getModule();
        }
        if (null == result) {
            for (Module module : getValidModules()) {
                if (PascalModuleType.isPascalModule(module)) {
                    return module;
                }
            }
        }
        return result;
    }

    @Nullable
    public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
        return new PascalCommandLineState(this, env, fixIOBuffering);
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        Sdk sdk = getSdk();
        if (sdk != null && !(sdk.getSdkType() instanceof FPCSdkType)) {
            throw new ExecutionException(ObjectPascalLocalize.runDebuggerUnsupportedSdk(sdk.getName()).get());
        }
        String executable = getExecutable(environment);
        return NativeDebugTarget.launch(Path.of(executable),
                getProgramArguments(),
                StringUtil.isEmptyOrSpaces(workingDirectory) ? null : Path.of(workingDirectory),
                envs,
                null).withSetup(PascalDebuggerSetup.INSTANCE);
    }

    public GeneralCommandLine createCommandLine(ExecutionEnvironment environment) throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine(getExecutable(environment));
        commandLine.addParameters(getProgramArguments());
        if (!StringUtil.isEmptyOrSpaces(workingDirectory)) {
            commandLine.withWorkDirectory(workingDirectory);
        }
        commandLine.withEnvironment(envs);
        commandLine.withParentEnvironmentType(passParentEnvs
                ? GeneralCommandLine.ParentEnvironmentType.CONSOLE
                : GeneralCommandLine.ParentEnvironmentType.NONE);
        return commandLine;
    }

    private String getExecutable(ExecutionEnvironment environment) throws ExecutionException {
        Module module = findModule(environment);
        if (module != null) {
            Path projectExecutable = ReadAction.compute(() -> {
                PascalBuildModuleExtension<?> extension = ModuleUtilCore.getExtension(module, PascalBuildModuleExtension.class);
                return extension != null ? extension.getExecutable() : null;
            });
            if (projectExecutable != null) {
                return projectExecutable.toString();
            }
        }
        String executable = null;
        if (module != null) {
            String fileName;
            if (programFileName != null) {
                fileName = FileUtil.getFilename(programFileName);
            } else {
                VirtualFile mainFile = PascalModuleType.getMainFile(module);
                fileName = mainFile != null ? mainFile.getNameWithoutExtension() : null;
            }
            executable = PascalRunner.getExecutable(module, fileName);
        }
        if (executable == null) {
            throw new ExecutionException(ObjectPascalLocalize.executionNoexecutable().get());
        }
        return executable;
    }

    private List<String> getProgramArguments() {
        return ParametersListUtil.parse(StringUtil.notNullize(parameters));
    }

    @Override
    public String getProgramParameters() {
        return parameters;
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public void setEnvs(@Nonnull Map<String, String> map) {
        envs = new LinkedHashMap<>(map);
    }

    @Nonnull
    @Override
    public Map<String, String> getEnvs() {
        return envs;
    }

    @Override
    public void setPassParentEnvs(boolean passParentEnvs) {
        this.passParentEnvs = passParentEnvs;
    }

    @Override
    public boolean isPassParentEnvs() {
        return passParentEnvs;
    }

    @Override
    public boolean getFixIOBuffering() {
        return fixIOBuffering;
    }

    @Override
    public boolean getDebugMode() {
        return debugMode;
    }

    @Override
    public void setProgramParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void setFixIOBuffering(boolean value) {
        fixIOBuffering = value;
    }

    @Override
    public void setDebugMode(boolean value) {
        debugMode = value;
    }

    @Override
    public String getModuleName() {
        return getConfigurationModule().getModuleName();
    }

    public String getProgramFileName() {
        return programFileName;
    }

    public void setProgramFileName(String programFileName) {
        this.programFileName = programFileName;
    }

    public Sdk getSdk() {
        Module module = getConfigurationModule().getModule();
        return module != null ? ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class) : null;
    }

    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        setProgramFileName(element.getAttributeValue(ATTR_PROGRAM_FILE_NAME));
        parameters = element.getAttributeValue(ATTR_PROGRAM_PARAMETERS);
        workingDirectory = element.getAttributeValue(ATTR_WORKING_DIRECTORY);
        passParentEnvs = !Boolean.FALSE.toString().equals(element.getAttributeValue(ATTR_PASS_PARENT_ENVS));
        envs = new LinkedHashMap<>();
        for (Element env : element.getChildren(ELEMENT_ENV)) {
            String name = env.getAttributeValue(ATTR_ENV_NAME);
            if (name != null) {
                envs.put(name, StringUtil.notNullize(env.getAttributeValue(ATTR_ENV_VALUE)));
            }
        }
    }

    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        if (programFileName != null) {
            element.setAttribute(ATTR_PROGRAM_FILE_NAME, programFileName);
        }
        if (parameters != null) {
            element.setAttribute(ATTR_PROGRAM_PARAMETERS, parameters);
        }
        if (workingDirectory != null) {
            element.setAttribute(ATTR_WORKING_DIRECTORY, workingDirectory);
        }
        if (!passParentEnvs) {
            element.setAttribute(ATTR_PASS_PARENT_ENVS, Boolean.FALSE.toString());
        }
        for (Map.Entry<String, String> entry : envs.entrySet()) {
            Element env = new Element(ELEMENT_ENV);
            env.setAttribute(ATTR_ENV_NAME, entry.getKey());
            env.setAttribute(ATTR_ENV_VALUE, entry.getValue());
            element.addContent(env);
        }
    }
}
