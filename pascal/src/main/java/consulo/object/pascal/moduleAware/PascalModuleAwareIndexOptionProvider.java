package consulo.object.pascal.moduleAware;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.lexer.PascalFlexLexerImpl;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.language.psi.stub.IndexOption;
import consulo.language.psi.stub.ModuleAwareIndexOptionProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.OrderEnumerator;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.object.pascal.module.extension.ObjectPascalModuleExtension;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class PascalModuleAwareIndexOptionProvider implements ModuleAwareIndexOptionProvider {
    private static final Set<FileType> FILE_TYPES = Set.of(PascalFileType.INSTANCE);
    private static final String INCLUDE_EXTENSION = "inc";
    private static final Pattern INCLUDE_DIRECTIVE = Pattern.compile("(?i)\\{\\$I(NCLUDE)?\\s");

    @Override
    public String getId() {
        return PascalDefineEnv.PROVIDER_ID;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public Set<FileType> getInputFileTypes() {
        return FILE_TYPES;
    }

    @Override
    public IndexOption getOptions(Module module, VirtualFile file) {
        Sdk sdk = ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class);
        return sdk != null ? toIndexOption(sdk) : IndexOption.fullySharable();
    }

    @Override
    public Map<VirtualFile, List<IndexOption>> analyze(Project project, @Nullable Collection<VirtualFile> changedFiles) {
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
        Collection<VirtualFile> files = changedFiles != null ? changedFiles : collectPascalFiles(project, fileIndex);
        PascalIncludeResolver includeResolver = new PascalIncludeResolver(fileIndex);
        Map<Module, Sdk> sdks = new HashMap<>();
        Map<VirtualFile, Map<PascalDefineOptions, IndexOption>> variants = new LinkedHashMap<>();
        Map<VirtualFile, Map<PascalDefineOptions, IndexOption>> includeVariants = new LinkedHashMap<>();
        for (VirtualFile file : files) {
            if (!file.isValid() || file.isDirectory() || file.getFileType() != PascalFileType.INSTANCE || isInclude(file)) {
                continue;
            }
            Map<PascalDefineOptions, IndexOption> hostVariants = hostVariants(file, fileIndex, sdks);
            if (fileIndex.getModuleForFile(file) == null && !hostVariants.isEmpty()) {
                variants.put(file, hostVariants);
            }
            collectIncludeVariants(file, hostVariants.keySet(), includeResolver, includeVariants);
        }
        variants.putAll(includeVariants);
        Map<VirtualFile, List<IndexOption>> result = new HashMap<>();
        for (Map.Entry<VirtualFile, Map<PascalDefineOptions, IndexOption>> entry : variants.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        return result;
    }

    private static Map<PascalDefineOptions, IndexOption> hostVariants(VirtualFile file, ProjectFileIndex fileIndex, Map<Module, Sdk> sdks) {
        Map<PascalDefineOptions, IndexOption> result = new LinkedHashMap<>();
        Module module = fileIndex.getModuleForFile(file);
        if (module != null) {
            addSdkVariant(sdks.computeIfAbsent(module, PascalModuleAwareIndexOptionProvider::sdkOf), result);
            return result;
        }
        for (OrderEntry entry : fileIndex.getOrderEntriesForFile(file)) {
            addSdkVariant(sdks.computeIfAbsent(entry.getOwnerModule(), PascalModuleAwareIndexOptionProvider::sdkOf), result);
        }
        return result;
    }

    private static void addSdkVariant(@Nullable Sdk sdk, Map<PascalDefineOptions, IndexOption> result) {
        if (sdk == null) {
            return;
        }
        PascalDefineOptions options = optionsOf(sdk);
        if (!result.containsKey(options)) {
            result.put(options, toIndexOption(sdk, options));
        }
    }

    private static void collectIncludeVariants(VirtualFile host, Collection<PascalDefineOptions> hostOptions, PascalIncludeResolver includeResolver,
                                               Map<VirtualFile, Map<PascalDefineOptions, IndexOption>> includeVariants) {
        CharSequence text;
        try {
            text = VirtualFileUtil.loadText(host);
        }
        catch (IOException e) {
            return;
        }
        if (!INCLUDE_DIRECTIVE.matcher(text).find()) {
            return;
        }
        Collection<PascalDefineOptions> initialStates = hostOptions.isEmpty() ? List.of(PascalDefineOptions.EMPTY) : hostOptions;
        LocalizeValue label = LocalizeValue.of(host.getName());
        for (PascalDefineOptions initial : initialStates) {
            for (Pair<VirtualFile, PascalDefineOptions> state : PascalFlexLexerImpl.collectIncludeStates(host, text, initial, includeResolver::resolve)) {
                Map<PascalDefineOptions, IndexOption> fileVariants = includeVariants.computeIfAbsent(state.getFirst(), file -> new LinkedHashMap<>());
                if (!fileVariants.containsKey(state.getSecond())) {
                    fileVariants.put(state.getSecond(), state.getSecond().defines().isEmpty()
                        ? IndexOption.fullySharable()
                        : IndexOption.sharablePerOption(state.getSecond(), PascalDefineOptionsExternalizer.INSTANCE, label));
                }
            }
        }
    }

    private static boolean isInclude(VirtualFile file) {
        return INCLUDE_EXTENSION.equalsIgnoreCase(file.getExtension());
    }

    @Nullable
    private static Sdk sdkOf(Module module) {
        return ModuleUtilCore.getSdk(module, ObjectPascalModuleExtension.class);
    }

    private static Collection<VirtualFile> collectPascalFiles(Project project, ProjectFileIndex fileIndex) {
        Set<VirtualFile> files = new LinkedHashSet<>();
        fileIndex.iterateContent(file -> {
            if (!file.isDirectory() && file.getFileType() == PascalFileType.INSTANCE) {
                files.add(file);
            }
            return true;
        });
        files.addAll(collectLibrarySources(project, fileIndex));
        return files;
    }

    private static Collection<VirtualFile> collectLibrarySources(Project project, ProjectFileIndex fileIndex) {
        Set<VirtualFile> roots = new LinkedHashSet<>();
        for (Module module : ModuleManager.getInstance(project).getSortedModules()) {
            for (VirtualFile root : OrderEnumerator.orderEntries(module).withoutModuleSourceEntries().sources().getRoots()) {
                roots.add(root);
            }
        }
        Set<VirtualFile> files = new LinkedHashSet<>();
        for (VirtualFile root : roots) {
            if (!root.isDirectory()) {
                if (root.getFileType() == PascalFileType.INSTANCE) {
                    files.add(root);
                }
                continue;
            }
            VirtualFileUtil.iterateChildrenRecursively(root, file -> !fileIndex.isExcluded(file), file -> {
                if (!file.isDirectory() && file.getFileType() == PascalFileType.INSTANCE && fileIndex.getModuleForFile(file) == null) {
                    files.add(file);
                }
                return true;
            });
        }
        return files;
    }

    private static IndexOption toIndexOption(Sdk sdk) {
        return toIndexOption(sdk, optionsOf(sdk));
    }

    private static IndexOption toIndexOption(Sdk sdk, PascalDefineOptions options) {
        if (options.defines().isEmpty()) {
            return IndexOption.fullySharable();
        }
        return IndexOption.sharablePerOption(options, PascalDefineOptionsExternalizer.INSTANCE, LocalizeValue.of(sdk.getName()));
    }

    private static PascalDefineOptions optionsOf(Sdk sdk) {
        String version = sdk.getVersionString();
        if (version == null) {
            return PascalDefineOptions.EMPTY;
        }
        return PascalDefineOptions.of(BasePascalSdkType.getDefaultDefines(sdk, version));
    }
}
