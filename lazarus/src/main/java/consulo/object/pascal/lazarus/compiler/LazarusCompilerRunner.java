package consulo.object.pascal.lazarus.compiler;

import com.siberika.idea.pascal.jps.builder.PascalCompilerMessager;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.application.progress.ProgressIndicator;
import consulo.build.ui.progress.BuildProgress;
import consulo.build.ui.progress.BuildProgressDescriptor;
import consulo.compiler.CompileContextEx;
import consulo.compiler.CompileDriver;
import consulo.compiler.CompilerMessageCategory;
import consulo.compiler.CompilerRunner;
import consulo.compiler.ExitException;
import consulo.compiler.ExitStatus;
import consulo.compiler.util.ModuleCompilerUtil;
import consulo.dataContext.DataContext;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.lazarus.LazarusBuild;
import consulo.object.pascal.lazarus.LazarusBuildCommand;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.lazarus.module.LazarusModuleExtension;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class LazarusCompilerRunner implements CompilerRunner {
    private static final YesResult YES = new YesResult(ObjectPascalIconGroup.lazarus());
    private static final long WAIT_INTERVAL = 100L;

    private final Project myProject;
    private final ModuleExtensionHelper myModuleExtensionHelper;
    private final ProcessHandlerBuilderFactory myProcessHandlerBuilderFactory;

    @Inject
    public LazarusCompilerRunner(Project project, ModuleExtensionHelper moduleExtensionHelper, ProcessHandlerBuilderFactory processHandlerBuilderFactory) {
        myProject = project;
        myModuleExtensionHelper = moduleExtensionHelper;
        myProcessHandlerBuilderFactory = processHandlerBuilderFactory;
    }

    @RequiredReadAction
    @Override
    public Result checkAvailable(DataContext dataContext) {
        return myModuleExtensionHelper.hasModuleExtension(LazarusModuleExtension.class) ? YES : NO;
    }

    @Override
    public LocalizeValue getName() {
        return LazarusLocalize.compilerName();
    }

    @Override
    public boolean build(CompileDriver compileDriver,
                         CompileContextEx context,
                         BuildProgress<BuildProgressDescriptor> buildProgress,
                         boolean isRebuild,
                         boolean forceCompile,
                         boolean onlyCheckStatus) throws ExitException {
        List<Module> modules = new ArrayList<>(List.of(context.getCompileScope().getAffectedModules()));
        ModuleCompilerUtil.sortModules(myProject, modules);

        List<LazarusBuildCommand> commands = new ArrayList<>();
        Set<Path> contentRoots = new LinkedHashSet<>();
        ReadAction.run(() -> {
            for (Module module : modules) {
                for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
                    contentRoots.add(Path.of(root.getPath()));
                }
                LazarusModuleExtension extension = ModuleRootManager.getInstance(module).getExtension(LazarusModuleExtension.class);
                if (extension != null && !extension.isPackage()) {
                    commands.add(LazarusBuild.create(extension, isRebuild));
                }
            }
        });

        boolean didSomething = false;
        for (LazarusBuildCommand command : commands) {
            if (!command.error().isEmpty()) {
                context.newError(command.error()).add();
                continue;
            }
            if (command.commandLine() != null) {
                run(context, command, contentRoots);
                didSomething = true;
            }
        }
        return didSomething;
    }

    private void run(CompileContextEx context, LazarusBuildCommand command, Set<Path> contentRoots) throws ExitException {
        GeneralCommandLine commandLine = command.commandLine();
        ProgressIndicator indicator = context.getProgressIndicator();
        indicator.setText(LazarusLocalize.compilerProgress(commandLine.getCommandLineString()));

        PascalCompilerMessager messager = new PascalCompilerMessager(context);
        Path workingDirectory = command.workingDirectory();
        if (workingDirectory != null) {
            messager.setWorkingDirectory(workingDirectory);
        }
        LazbuildOutputParser parser = new LazbuildOutputParser(messager, contentRoots, indicator);
        int errorsBefore = context.getMessageCount(CompilerMessageCategory.ERROR);

        ProcessHandler handler;
        try {
            handler = myProcessHandlerBuilderFactory.newBuilder(commandLine).build();
        }
        catch (ExecutionException e) {
            context.newError(LocalizeValue.of(e.getMessage())).add();
            return;
        }
        handler.addProcessListener(parser);
        handler.startNotify();
        while (!handler.waitFor(WAIT_INTERVAL)) {
            if (indicator.isCanceled()) {
                handler.destroyProcess();
                handler.waitFor();
                throw new ExitException(ExitStatus.CANCELLED);
            }
        }

        Integer exitCode = handler.getExitCode();
        if (exitCode != null && exitCode != 0 && context.getMessageCount(CompilerMessageCategory.ERROR) == errorsBefore) {
            List<String> buildErrors = parser.getBuildErrors();
            if (buildErrors.isEmpty()) {
                context.newError(LazarusLocalize.compilerExitCode(commandLine.getExePath(), exitCode)).add();
            }
            else {
                for (String error : buildErrors) {
                    context.newError(LocalizeValue.of(error)).add();
                }
            }
        }
    }
}
