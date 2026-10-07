package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.module.PascalModuleType;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.ConfigurationFromContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.execution.configuration.RunConfiguration;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.module.Module;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
@ExtensionImpl
public class PascalRunContextConfigurationProducer extends RunConfigurationProducer<PascalRunConfiguration> implements Cloneable {
    public PascalRunContextConfigurationProducer() {
        super(PascalConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(PascalRunConfiguration configuration, ConfigurationContext context, SimpleReference<PsiElement> sourceElement) {
        if (isProgramLeafElement(sourceElement.get())) {
            setupConf(context, configuration, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(PascalRunConfiguration configuration, ConfigurationContext context) {
        return (configuration.getConfigurationModule().getModule() == context.getModule()) &&
                (context.getPsiLocation() != null) &&
                (configuration.getProgramFileName() == null || (configuration.getProgramFileName().equals(getProgramFileName(context))));
    }

    private String getProgramFileName(@NotNull ConfigurationContext context) {
        VirtualFile mainFile = context.getPsiLocation() != null ? getMainFile(context.getPsiLocation()) : null;
        return mainFile != null ? mainFile.getNameWithoutExtension() : null;
    }

    private void setupConf(ConfigurationContext context, RunConfiguration conf, boolean setupModule) {
        if (conf instanceof PascalRunConfiguration) {
            conf.setName(context.getProject().getName());
            Module module = context.getModule();
            if (PascalModuleType.isPascalModule(module) && context.getPsiLocation() != null) {
                conf.setName(module.getName());
                PascalRunConfiguration pasConf = (PascalRunConfiguration) conf;
                pasConf.setModule(module);
                pasConf.setProgramFileName(getProgramFileName(context));
                VirtualFile mainFile = getMainFile(context.getPsiLocation());
                if (mainFile != null) {
                    conf.setName(String.format("[%s] %s", module.getName(), mainFile.getNameWithoutExtension()));
                    if (setupModule) {
                        PascalModuleType.setMainFile(module, mainFile);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public ConfigurationFromContext createConfigurationFromContext(ConfigurationContext context) {
        return isProgramLeafElement(context.getPsiLocation()) ? super.createConfigurationFromContext(context) : null;
    }

    private static VirtualFile getMainFile(@NotNull PsiElement element) {
        PsiFile mainFile = element.getContainingFile();
        return mainFile != null ? mainFile.getVirtualFile() : null;
    }

    public static boolean isProgramLeafElement(PsiElement element) {
        ASTNode node = element.getNode();
        if ((null == node) || ((node.getElementType() != PasTypes.BEGIN) && (node.getElementType() != PasTypes.PROGRAM))) {
            return false;
        }
        if ((element.getFirstChild() == null) && (element.getParent().getFirstChild() == element) &&
                (element.getParent() instanceof PasProgramModuleHead
                        || element.getParent().getParent() instanceof PasBlockBody && element.getParent().getParent().getParent() instanceof PasBlockGlobal)
                ) {
            PasModule module = PsiUtil.getElementPasModule(element);
            return module != null && module.getModuleType() == PascalModule.ModuleType.PROGRAM;
        }
        return false;
    }
}