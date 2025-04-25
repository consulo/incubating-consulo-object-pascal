package com.siberika.idea.pascal.module;

import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.jps.util.SysUtils;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.ApplicationManager;
import consulo.application.util.function.Processor;
import consulo.component.ProcessCanceledException;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FileTypeIndex;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@ServiceAPI(ComponentScope.MODULE)
@ServiceImpl
@Singleton
public class ModuleService {

    private static final Logger LOG = Logger.getInstance(ModuleService.class);

    private static final ModuleService INSTANCE_DEFAULT = new ModuleService();

    private static final long CACHE_TTL_MS = 30000;

    private final Map<Object, SmartPsiElementPointer> cache = new ConcurrentHashMap<>();
    private long lastClearTime = System.nanoTime();
    private final ReentrantLock cacheNameFileLock = new ReentrantLock();
    private final Map<String, VirtualFile> cacheNameFileMap = new ConcurrentHashMap<>();
    private long lastClearTimeNameFile = 0;
    private File syntaxCheckTempDir;

    public ModuleService() {
        syntaxCheckTempDir = SysUtils.createTempDir("ipassynck");
    }

    public static ModuleService getInstance(@Nullable Module module) {
        if (module != null) {
            return module.getComponent(ModuleService.class);
        }
        else {
            return INSTANCE_DEFAULT;
        }
    }

    public static void ensureNameFileCache(VirtualFile file, Project project, boolean checkTTL) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        ModuleService.getInstance(module).ensureCache(module, checkTTL);
    }

    public <K, V extends PsiElement> V calcWithCache(K key, Callable<V> callable) {
        V result = null;
        if (ensureCacheFresh()) {
            SmartPsiElementPointer resultPtr = cache.get(key);
            if (resultPtr != null) {
                result = (V) resultPtr.getElement();
                if (!PsiUtil.isElementUsable(result)) {
                    LOG.info("--- Cached value became invalid");
                    result = null;
                }
            }
        }
        if (null == result) {
            try {
                result = callable.call();
                if (result != null) {
                    cache.put(key, PsiUtil.createSmartPointer(result));
                }
            }
            catch (ProcessCanceledException e) {
                throw e;
            }
            catch (Exception e) {
                LOG.error("Error calculating value", e);
            }
        }
        return result;
    }

    private boolean ensureCacheFresh() {
        long currentTime = System.nanoTime();
        if (currentTime - lastClearTime > CACHE_TTL_MS) {
            cache.clear();
            lastClearTime = currentTime;
            return false;
        }
        else {
            return true;
        }
    }

    public VirtualFile getFileByUnitName(String unitName) {
        return SyncUtil.doWithLock(cacheNameFileLock, () -> cacheNameFileMap.get(unitName));
    }

    public void ensureCache(@Nullable Module module, boolean checkTTL) {
        if (null == module) {
            return;
        }
        long currentTime = System.nanoTime();
        if ((lastClearTimeNameFile == 0) || (checkTTL && ((currentTime - lastClearTimeNameFile) > CACHE_TTL_MS))) {
            fillCache(module);
        }
    }

    private void fillCache(@NotNull Module module) {
        if (DumbService.isDumb(module.getProject())) {
            return;
        }
        SyncUtil.doWithLock(cacheNameFileLock, () -> {
            cacheNameFileMap.clear();
            lastClearTimeNameFile = System.nanoTime();
            ;
            ApplicationManager.getApplication().runReadAction(() -> {
                FileTypeIndex.processFiles(PPUFileType.INSTANCE, new Processor<VirtualFile>() {
                    @Override
                    public boolean process(VirtualFile file) {
                        cacheNameFileMap.put(file.getNameWithoutExtension(), file);
                        return true;
                    }
                }, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
            });
        });
    }

    public File getSyntaxCheckTempDir() {
        return syntaxCheckTempDir;
    }
}
