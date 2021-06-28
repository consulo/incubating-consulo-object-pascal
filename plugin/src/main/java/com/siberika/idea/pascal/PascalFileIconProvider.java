package com.siberika.idea.pascal;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Processors;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.stub.PascalModuleIndex;
import consulo.annotation.access.RequiredReadAction;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;

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
        String ext = FileUtilRt.getExtension(file.getName());
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
