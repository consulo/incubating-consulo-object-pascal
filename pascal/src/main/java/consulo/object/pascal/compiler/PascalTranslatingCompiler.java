package consulo.object.pascal.compiler;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.jps.builder.FPCCompilerProcessAdapter;
import com.siberika.idea.pascal.jps.builder.PascalCompilerMessager;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.PascalBackendCompiler;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.compiler.CompileContext;
import consulo.compiler.CompilerMessageCategory;
import consulo.compiler.ModuleCompilerPathsManager;
import consulo.compiler.TranslatingCompiler;
import consulo.compiler.scope.CompileScope;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.object.pascal.module.extension.PascalModuleBuild;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessAdapter;
import consulo.process.local.ProcessHandlerFactory;
import consulo.util.collection.Chunk;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeRegistry;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 12/07/2021
 */
@ExtensionImpl
public class PascalTranslatingCompiler implements TranslatingCompiler {
    private static final Logger LOG = Logger.getInstance(PascalTranslatingCompiler.class);

    @Override
    public boolean isCompilableFile(Path file, CompileContext context) {
        return FileTypeRegistry.getInstance().getFileTypeByFileName(file.getFileName().toString()) == PascalFileType.INSTANCE;
    }

    @Override
    public void compile(CompileContext context, Chunk<Module> moduleChunk, Collection<Path> files, OutputSink sink) {
        Module module = moduleChunk.getNodes().iterator().next();

        PascalModuleBuild projectBuild = ReadAction.compute(() -> {
            ObjectPascalModuleExtension<?> extension = ModuleUtilCore.getExtension(module, ObjectPascalModuleExtension.class);
            return extension != null ? extension.createBuild() : null;
        });
        if (projectBuild != null) {
            buildWithProjectSystem(context, projectBuild);
            return;
        }

        Sdk sdk = ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class);

        PascalSdkData sdkData = BasePascalSdkType.getAdditionalData(sdk);

        String family = sdkData.getString(PascalSdkData.Keys.COMPILER_FAMILY);

        PascalCompilerMessager messager = new PascalCompilerMessager(context);

        PascalBackendCompiler compiler = family != null ? PascalBackendCompiler.getCompiler(PascalCompilerFamily.of(family), messager) : null;
        if (compiler == null) {
            context.newError(LocalizeValue.localizeTODO("Pascal compiler is not configured for module '" + module.getName() + "'")).add();
            return;
        }

        Path outputPath = ModuleCompilerPathsManager.getInstance(module).getCompilerOutputPath(ProductionContentFolderTypeProvider.getInstance());
        if (outputPath == null) {
            context.newError(LocalizeValue.localizeTODO("Compiler output path is not configured for module '" + module.getName() + "'")).add();
            return;
        }

        FileUtil.createDirectory(outputPath.toFile());
        try {
            ObjectPascalModuleExtension extension = ModuleUtilCore.getExtension(module, ObjectPascalModuleExtension.class);
            String mainFilePath = extension != null ? extension.getMainFilePath() : null;
            List<File> inputFiles = new ArrayList<>(files.size() + 1);
            if (mainFilePath != null) {
                inputFiles.add(new File(mainFilePath));
            }
            for (Path file : files) {
                inputFiles.add(file.toFile());
            }

            List<File> moduleSourceRoots = ReadAction.compute(() -> VirtualFileUtil.virtualToIoFiles(
                List.of(ModuleRootManager.getInstance(module).getContentFolderFiles(LanguageContentFolderScopes.production()))));

            Set<File> sdkFiles = new LinkedHashSet<>();
            sdkFiles.addAll(VirtualFileUtil.virtualToIoFiles(List.of(sdk.getRootProvider().getFiles(BinariesOrderRootType.ID))));
            sdkFiles.addAll(VirtualFileUtil.virtualToIoFiles(List.of(sdk.getRootProvider().getFiles(SourcesOrderRootType.ID))));

            String[] command = compiler.createStartupCommand(sdk.getHomePath(), module.getName(), outputPath.toString(), new ArrayList<>(sdkFiles), moduleSourceRoots, inputFiles, null, true, true, sdkData.toParamMap());

            launchCompiler(compiler, messager, command);
        }
        catch (Exception e) {
            LOG.error(e);
        }
    }

    private static void buildWithProjectSystem(CompileContext context, PascalModuleBuild build) {
        if (!build.error().isEmpty()) {
            context.newError(build.error()).add();
            return;
        }
        GeneralCommandLine commandLine = build.commandLine();
        if (commandLine == null) {
            return;
        }
        PascalCompilerMessager messager = new PascalCompilerMessager(context);
        messager.info(null, commandLine.getCommandLineString(), null, -1L, -1L);
        try {
            ProcessHandler handler = context.getProject().getApplication().getInstance(ProcessHandlerBuilderFactory.class)
                .newBuilder(commandLine)
                .build();
            handler.addProcessListener(new FPCCompilerProcessAdapter(messager));
            handler.startNotify();
            handler.waitFor();
            Integer exitCode = handler.getExitCode();
            if (exitCode != null && exitCode != 0 && context.getMessageCount(CompilerMessageCategory.ERROR) == 0) {
                context.newError(LocalizeValue.localizeTODO(commandLine.getExePath() + " exited with code " + exitCode)).add();
            }
        }
        catch (ExecutionException e) {
            context.newError(LocalizeValue.of(e)).add();
        }
    }

    private int launchCompiler(PascalBackendCompiler compiler, CompilerMessager messager, String[] cmdLine) throws Exception {
        messager.info(null, "Command line: ", null, -1L, -1L);
        for (String s : cmdLine) {
            messager.info(null, s, null, -1L, -1L);
        }
        GeneralCommandLine commandLine = new GeneralCommandLine(cmdLine);

        ProcessHandler handler = ProcessHandlerFactory.getInstance().createProcessHandler(commandLine);
        ProcessAdapter adapter = compiler.getCompilerProcessAdapter(messager);
        handler.addProcessListener(adapter);
        handler.startNotify();
        handler.waitFor();
        return handler.getExitCode();
    }

    @Nonnull
    @Override
    public FileType[] getInputFileTypes() {
        return new FileType[]{PascalFileType.INSTANCE};
    }

    @Nonnull
    @Override
    public FileType[] getOutputFileTypes() {
        return new FileType[0];
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Pascal compiler";
    }

    @Override
    public boolean validateConfiguration(CompileScope compileScope) {
        return true;
    }
}
