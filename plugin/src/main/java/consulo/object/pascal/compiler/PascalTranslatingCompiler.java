package consulo.object.pascal.compiler;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.TranslatingCompiler;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Chunk;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.jps.builder.PascalCompilerMessager;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.PascalBackendCompiler;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.compiler.ModuleCompilerPathsManager;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.roots.impl.ProductionContentFolderTypeProvider;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.SourcesOrderRootType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 12/07/2021
 */
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
        String outputPath = VfsUtil.urlToPath(outputUrl);

        FileUtil.createDirectory(new File(outputPath));
        try {
            List<File> inputFilesAsList = VfsUtil.virtualToIoFiles(List.of(inputFiles));

            Set<File> sdkFiles = new LinkedHashSet<>();
            sdkFiles.addAll(VfsUtil.virtualToIoFiles(List.of(sdk.getRootProvider().getFiles(BinariesOrderRootType.getInstance()))));
            sdkFiles.addAll(VfsUtil.virtualToIoFiles(List.of(sdk.getRootProvider().getFiles(SourcesOrderRootType.getInstance()))));

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

        OSProcessHandler handler = ProcessHandlerFactory.getInstance().createProcessHandler(commandLine);
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
