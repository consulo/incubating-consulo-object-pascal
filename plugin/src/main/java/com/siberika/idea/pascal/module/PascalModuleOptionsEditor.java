package com.siberika.idea.pascal.module;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import consulo.configurable.ConfigurationException;
import consulo.content.CollectingContentIterator;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.ide.setting.module.ModuleConfigurationState;
import consulo.module.Module;
import consulo.module.content.ModuleFileIndex;
import consulo.module.content.ModuleRootManager;
import consulo.ui.ex.awt.ComboBox;
import consulo.ui.ex.awt.TextBrowseFolderListener;
import consulo.ui.ex.awt.TextFieldWithBrowseButton;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 14/01/2013
 */
public class PascalModuleOptionsEditor {
    final ModuleConfigurationState state;
    final Module module;

    private JComponent myComponent;
    private TextFieldWithBrowseButton exePathEdit;
    private JComboBox mainFileCBox;

    public PascalModuleOptionsEditor(ModuleConfigurationState state, Module module) {
        assert PascalModuleType.isPascalModule(module);
        this.state = state;
        this.module = module;
    }

    public void saveData() {
    }

    public void moduleStateChanged() {
    }

    @Nls
    public String getDisplayName() {
        return PascalBundle.message("ui.module.options.editor.name");
    }

    @Nullable
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    public JComponent createComponent() {
        if (myComponent == null) {
            myComponent = createComponentImpl();
        }
        return myComponent;
    }

    private JComponent createComponentImpl() {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel(PascalBundle.message("ui.module.options.editor.mainFile.label")),
                new GridBagConstraints(0,0,1,1,0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 6, 6, 0), 0, 0));
        panel.add(new JLabel(PascalBundle.message("ui.module.options.editor.exePath.label")),
                new GridBagConstraints(0,1,1,1,0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 6, 6, 0), 0, 0));

        final ModuleFileIndex index = ModuleRootManager.getInstance(module).getFileIndex();
        final List<VirtualFile> pascalFiles = new ArrayList<VirtualFile>();
        index.iterateContent(new CollectingContentIterator() {
            @NotNull
            @Override
            public List<VirtualFile> getFiles() {
                return pascalFiles;
            }

            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                if (fileOrDir.isValid() && !fileOrDir.isDirectory()
                    && PascalFileType.INSTANCE.equals(fileOrDir.getFileType())) {
                    pascalFiles.add(fileOrDir);
                }
                return true;
            }
        });

        mainFileCBox = new ComboBox(pascalFiles.toArray());
        panel.add(mainFileCBox,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 6, 6), 0, 0));

        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        //noinspection DialogTitleCapitalization
        fileChooserDescriptor.setTitle(PascalBundle.message("title.choose.directory"));
        exePathEdit = new TextFieldWithBrowseButton();
        exePathEdit.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));

        panel.add(exePathEdit, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 6, 6), 0, 0));

        return panel;
    }

    public boolean isModified() {
        return true;
    }

    public void apply() throws ConfigurationException {
        PascalModuleType.setMainFile(module, (VirtualFile) mainFileCBox.getSelectedItem());
        PascalModuleType.setExeOutputPath(module, exePathEdit.getText());
    }

    public void reset() {
        mainFileCBox.setSelectedItem(PascalModuleType.getMainFile(module));
        exePathEdit.setText(PascalModuleType.getExeOutputPath(module));
    }

    public void disposeUIResources() {
    }
}
