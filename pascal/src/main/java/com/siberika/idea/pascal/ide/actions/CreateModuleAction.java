package com.siberika.idea.pascal.ide.actions;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalIcons;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateManager;
import consulo.fileTemplate.FileTemplateUtil;
import consulo.ide.action.CreateFileFromTemplateDialog;
import consulo.ide.action.CreateTemplateInPackageAction;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.module.content.DirectoryIndex;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * Author: George Bakhtadze
 * Date: 23/03/2015
 */
public class CreateModuleAction extends CreateTemplateInPackageAction<PsiFile> {
    private static final String PASCAL_TEMPLATE_PREFIX = "Pascal";

    public CreateModuleAction() {
        super(ObjectPascalLocalize.actionCreateNewModule(),
            ObjectPascalLocalize.actionCreateNewModule(),
            PascalIcons.GENERAL,
            true
        );
    }

    @Nullable
    @Override
    protected PsiElement getNavigationElement(@NotNull PsiFile createdElement) {
        return createdElement.getNavigationElement();
    }

    @Override
    protected boolean checkPackageExists(PsiDirectory directory) {
        return DirectoryIndex.getInstance(directory.getProject()).getPackageName(directory.getVirtualFile()) != null;
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(ObjectPascalLocalize.actionCreateNewModule());
        for (FileTemplate fileTemplate : getApplicableTemplates(directory.getProject())) {
            final String templateName = fileTemplate.getName();
            final String shortName = getTemplateShortName(templateName);
            final Image icon = getPopupTemplateIcon();
            builder.addKind(LocalizeValue.of(shortName), icon, templateName);
        }
    }

    @Override
    protected LocalizeValue getActionName(PsiDirectory directory, String newName, String templateName) {
        return ObjectPascalLocalize.progressCreatingModule();
    }

    @Nullable
    @Override
    protected PsiFile doCreate(@NotNull PsiDirectory dir, String className, String templateName) throws IncorrectOperationException {
        String packageName = DirectoryIndex.getInstance(dir.getProject()).getPackageName(dir.getVirtualFile());
        try {
            return createClass(className, packageName, dir, templateName).getContainingFile();
        }
        catch (Throwable e) {
            throw new IncorrectOperationException(e.getMessage());
        }
    }

    private static PsiElement createClass(String className, String packageName, @NotNull PsiDirectory directory, final String templateName)
        throws Exception {
        return createClass(className, packageName, directory, templateName, CreateModuleAction.class.getClassLoader());
    }

    private static PsiElement createClass(String className, String packageName, PsiDirectory directory, String templateName, @Nullable ClassLoader classLoader)
        throws Exception {
        final Properties props = new Properties(FileTemplateManager.getInstance(directory.getProject()).getDefaultProperties());
        props.setProperty(FileTemplate.ATTRIBUTE_NAME, className);
        props.setProperty(FileTemplate.ATTRIBUTE_PACKAGE_NAME, packageName);

        final FileTemplate template = FileTemplateManager.getInstance(directory.getProject()).getInternalTemplate(templateName);

        return FileTemplateUtil.createFromTemplate(template, className, props, directory, classLoader);
    }

    private static List<FileTemplate> getApplicableTemplates(Project project) {
        return getApplicableTemplates(project, fileTemplate -> PascalFileType.INSTANCE.getDefaultExtension().equals(fileTemplate.getExtension()));
    }

    private static List<FileTemplate> getApplicableTemplates(Project project, Predicate<FileTemplate> filter) {
        List<FileTemplate> applicableTemplates = new SmartList<FileTemplate>();
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getInternalTemplates(), filter));
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getAllTemplates(), filter));
        return applicableTemplates;
    }

    private static String getTemplateShortName(String templateName) {
        if (templateName.startsWith(PASCAL_TEMPLATE_PREFIX)) {
            return templateName.substring(PASCAL_TEMPLATE_PREFIX.length());
        }
        return templateName;
    }

    @NotNull
    private static Image getPopupTemplateIcon() {
        return PascalIcons.GENERAL;
    }
}
