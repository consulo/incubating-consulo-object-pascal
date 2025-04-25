package consulo.object.pascal.compiler;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.jps.builder.PascalCompilerMessager;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.PascalBackendCompiler;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.compiler.CompileContext;
import consulo.compiler.ModuleCompilerPathsManager;
import consulo.compiler.TranslatingCompiler;
import consulo.compiler.scope.CompileScope;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.process.ProcessHandler;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessAdapter;
import consulo.process.local.ProcessHandlerFactory;
import consulo.util.collection.Chunk;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 12/07/2021
 */
@ExtensionImpl
public class PascalTranslatingCompiler implements TranslatingCompiler {
    @Override
    public boolean isCompilableFile(VirtualFile virtualFile, CompileContext compileContext) {
        return virtualFile.getFileType() == PascalFileType.INSTANCE;
    }

    @Override
    public void compile(CompileContext compileContext, Chunk<Module> chunk, VirtualFile[] inputFiles, OutputSink outputSink) {
        Module module = chunk.getNodes().iterator().next();

        Sdk sdk = ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class);

        final PascalSdkData sdkData = BasePascalSdkType.getAdditionalData(sdk);

        String family = sdkData.getString(PascalSdkData.Keys.COMPILER_FAMILY);

        PascalCompilerMessager messager = new PascalCompilerMessager(compileContext);

        PascalBackendCompiler compiler = family != null ? PascalBackendCompiler.getCompiler(PascalCompilerFamily.of(family), messager) : null;

        String outputUrl = ModuleCompilerPathsManager.getInstance(module).getCompilerOutputUrl(ProductionContentFolderTypeProvider.getInstance());
        String outputPath = VirtualFileUtil.urlToPath(outputUrl);

        FileUtil.createDirectory(new File(outputPath));
        try {
            List<File> inputFilesAsList = VirtualFileUtil.virtualToIoFiles(List.of(inputFiles));

            Set<File> sdkFiles = new LinkedHashSet<>();
            sdkFiles.addAll(VirtualFileUtil.virtualToIoFiles(List.of(sdk.getRootProvider().getFiles(BinariesOrderRootType.getInstance()))));
            sdkFiles.addAll(VirtualFileUtil.virtualToIoFiles(List.of(sdk.getRootProvider().getFiles(SourcesOrderRootType.getInstance()))));

            String[] command = compiler.createStartupCommand(sdk.getHomePath(), module.getName(), outputPath, new ArrayList<>(sdkFiles), List.of(), inputFilesAsList, null, true, true, null);

            launchCompiler(compiler, messager, command);
        }
        catch (Exception e) {
            e.printStackTrace();
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
