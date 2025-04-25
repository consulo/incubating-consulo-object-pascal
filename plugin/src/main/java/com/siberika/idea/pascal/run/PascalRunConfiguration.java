package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.module.PascalModuleType;
import consulo.content.bundle.Sdk;
import consulo.execution.configuration.*;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.process.ExecutionException;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import jakarta.annotation.Nonnull;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 06/01/2013
 */
public class PascalRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
        implements PascalRunConfigurationParams, RunConfigurationWithSuppressedDefaultRunAction {

    private static final String ATTR_PROGRAM_FILE_NAME = "program_file_name";

    private String parameters;
    private String workingDirectory;
    private String programFileName;
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
        return new PascalCommandLineState(this, env, executor instanceof DefaultDebugExecutor, workingDirectory, parameters, fixIOBuffering);
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

    }

    @Nonnull
    @Override
    public Map<String, String> getEnvs() {
        return Map.of();
    }

    @Override
    public void setPassParentEnvs(boolean b) {

    }

    @Override
    public boolean isPassParentEnvs() {
        return false;
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
    }

    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        if (programFileName != null) {
            element.setAttribute(ATTR_PROGRAM_FILE_NAME, programFileName);
        }
    }
}