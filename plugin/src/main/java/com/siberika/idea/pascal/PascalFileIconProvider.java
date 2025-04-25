package com.siberika.idea.pascal;

import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.stub.PascalModuleIndex;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processors;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.collection.SmartList;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ExtensionImpl
public class PascalFileIconProvider implements IconDescriptorUpdater {
    @RequiredReadAction
    @Override
    public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement psiElement, int flags) {
        if(psiElement instanceof PsiFile) {
            Image icon = getIcon(PsiUtilCore.getVirtualFile(psiElement), flags, psiElement.getProject());
            if(icon != null) {
                iconDescriptor.setMainIcon(icon);
            }
        }
    }

    @Nullable
    public Image getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        String ext = FileUtil.getExtension(file.getName());
        if (PascalFileType.PROGRAM_EXTENSIONS.contains(ext)) {
            return PascalIcons.FILE_PROGRAM;
        } else if ("inc".equalsIgnoreCase(ext)) {
            return PascalIcons.FILE_INCLUDE;
        } else if (project != null) {
            if(DumbService.isDumb(project)) {
                return null;
            }
            Collection<PascalModule> modules = new SmartList<>();
            StubIndex.getInstance().processElements(PascalModuleIndex.KEY, file.getNameWithoutExtension().toUpperCase(), project, GlobalSearchScope.allScope(project),
                    PascalModule.class, Processors.cancelableCollectProcessor(modules));
            for (PascalModule module : modules) {
                if (file.getName().equalsIgnoreCase(module.getContainingFile().getName())) {
                    PascalModule.ModuleType moduleType = module.getModuleType();
                    if (moduleType == PascalModule.ModuleType.UNIT) {
                        return null;
                    } else if ((moduleType == PascalModule.ModuleType.LIBRARY) || (moduleType == PascalModule.ModuleType.PACKAGE)) {
                        return PascalIcons.FILE_LIBRARY;
                    } else if (moduleType == PascalModule.ModuleType.PROGRAM) {
                        return PascalIcons.FILE_PROGRAM;
                    } else {
                        return PascalIcons.FILE_INCLUDE;
                    }
                }
            }
        }
        return null;
    }
}
