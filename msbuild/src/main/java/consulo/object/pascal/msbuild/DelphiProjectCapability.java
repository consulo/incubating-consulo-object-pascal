package consulo.object.pascal.msbuild;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.sdk.DelphiSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.content.base.ExcludedContentFolderTypeProvider;
import consulo.content.bundle.Sdk;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.msbuild.MSBuildEvaluatedItem;
import consulo.msbuild.MSBuildProcessProvider;
import consulo.msbuild.MSBuildProjectCapability;
import consulo.object.pascal.module.extension.PascalModuleExtension;
import consulo.object.pascal.module.extension.PascalMutableModuleExtension;
import consulo.object.pascal.msbuild.module.DelphiMutableModuleExtension;
import consulo.object.pascal.sdk.PascalModuleSdkUtil;
import consulo.platform.Platform;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileTypeRegistry;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class DelphiProjectCapability implements MSBuildProjectCapability {
    public static final String ID = "Delphi";

    public static final Key<String> NEW_PROJECT_SDK_NAME = Key.create("DelphiProjectCapability.NEW_PROJECT_SDK_NAME");

    private static final Logger LOG = Logger.getInstance(DelphiProjectCapability.class);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void importModule(Module module,
                             ModifiableRootModel rootModel,
                             VirtualFile projectFile,
                             MSBuildProcessProvider buildProcessProvider,
                             Sdk msBuildSdk,
                             Map<String, String> properties,
                             List<? extends MSBuildEvaluatedItem> referencePaths,
                             Set<String> targets) {
        DelphiMutableModuleExtension extension = rootModel.getExtensionWithoutCheck(DelphiMutableModuleExtension.class);
        PascalMutableModuleExtension pascalExtension = rootModel.getExtensionWithoutCheck(PascalMutableModuleExtension.class);
        VirtualFile projectDir = projectFile.getParent();
        if (extension == null || pascalExtension == null || projectDir == null) {
            return;
        }

        Path projectDirPath = Path.of(projectDir.getPath());
        DelphiProjectModel model = DelphiProjectModel.of(projectDirPath, properties, Platform.current().os().isWindows());

        extension.setEnabled(true);
        extension.setProjectFilePath(projectFile.getPath());
        extension.setMainFilePath(pathString(model.mainSource()));
        extension.setConfiguration(model.config());
        extension.setPlatform(model.platform());
        extension.setOutputPath(pathString(model.exeOutput()));
        extension.setExecutablePath(pathString(model.executable()));

        Set<String> contentUrls = new HashSet<>();
        for (ContentEntry entry : rootModel.getContentEntries()) {
            contentUrls.add(entry.getUrl());
        }

        for (Path dir : model.unitSearchPath()) {
            addSourceFiles(rootModel, dir, contentUrls);
        }

        excludeOutput(rootModel, projectDirPath, model.dcuOutput(), contentUrls);
        excludeOutput(rootModel, projectDirPath, model.exeOutput(), contentUrls);

        pascalExtension.setEnabled(true);
        String sdkName = ReadAction.compute(() -> {
            PascalModuleExtension previous = ModuleRootManager.getInstance(module).getExtension(PascalModuleExtension.class);
            return previous != null ? previous.getInheritableSdk().getName() : null;
        });
        if (sdkName == null) {
            sdkName = module.getProject().getUserData(NEW_PROJECT_SDK_NAME);
        }
        if (sdkName != null) {
            pascalExtension.getInheritableSdk().set(null, sdkName);
        }
        else {
            Sdk sdk = PascalModuleSdkUtil.findDefaultSdk(DelphiSdkType.getInstance(), FPCSdkType.getInstance());
            if (sdk != null) {
                pascalExtension.getInheritableSdk().set(null, sdk);
            }
        }
        PascalModuleSdkUtil.ensureSdkEntry(rootModel, pascalExtension);
    }

    private static void addSourceFiles(ModifiableRootModel rootModel, Path dir, Set<String> contentUrls) {
        if (!Files.isDirectory(dir)) {
            return;
        }
        List<Path> files = new ArrayList<>();
        try (Stream<Path> children = Files.list(dir)) {
            children.filter(Files::isRegularFile).sorted().forEach(files::add);
        }
        catch (IOException e) {
            LOG.warn("Cannot list unit search path " + dir, e);
            return;
        }
        FileTypeRegistry fileTypeRegistry = FileTypeRegistry.getInstance();
        for (Path file : files) {
            if (fileTypeRegistry.getFileTypeByFileName(file.getFileName().toString()) != PascalFileType.INSTANCE) {
                continue;
            }
            String url = VirtualFileUtil.pathToUrl(file.toString());
            if (contentUrls.add(url)) {
                rootModel.addSingleContentEntry(url);
            }
        }
    }

    private static void excludeOutput(ModifiableRootModel rootModel, Path projectDir, @Nullable Path dir, Set<String> contentUrls) {
        if (dir == null || dir.equals(projectDir) || !dir.startsWith(projectDir)) {
            return;
        }
        String url = VirtualFileUtil.pathToUrl(dir.toString());
        if (contentUrls.add(url)) {
            rootModel.addContentEntry(url).addFolder(url, ExcludedContentFolderTypeProvider.getInstance());
        }
    }

    @Nullable
    private static String pathString(@Nullable Path path) {
        return path != null ? path.toString() : null;
    }
}
