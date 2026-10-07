package consulo.object.pascal.msbuild.module;

import consulo.annotation.access.RequiredReadAction;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionBase;
import consulo.object.pascal.module.extension.PascalBuildModuleExtension;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nullable;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class DelphiModuleExtension extends ModuleExtensionBase<DelphiModuleExtension> implements PascalBuildModuleExtension<DelphiModuleExtension> {
    public static final String ID = "delphi-msbuild";

    private static final String ATTR_PROJECT_FILE = "project-file";
    private static final String ATTR_MAIN_FILE = "main-file";
    private static final String ATTR_CONFIGURATION = "configuration";
    private static final String ATTR_PLATFORM = "platform";
    private static final String ATTR_OUTPUT = "output";
    private static final String ATTR_EXECUTABLE = "executable";

    @Nullable
    protected String myProjectFilePath;
    @Nullable
    protected String myMainFilePath;
    @Nullable
    protected String myConfiguration;
    @Nullable
    protected String myPlatform;
    @Nullable
    protected String myOutputPath;
    @Nullable
    protected String myExecutablePath;

    public DelphiModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @Nullable
    public String getProjectFilePath() {
        return myProjectFilePath;
    }

    @Nullable
    @Override
    public String getMainFilePath() {
        return myMainFilePath;
    }

    @Nullable
    public String getConfiguration() {
        return myConfiguration;
    }

    @Nullable
    public String getPlatform() {
        return myPlatform;
    }

    @Nullable
    @Override
    public String getOutputPath() {
        return myOutputPath;
    }

    @Nullable
    public String getExecutablePath() {
        return myExecutablePath;
    }


    @Nullable
    @RequiredReadAction
    @Override
    public Path getExecutable() {
        String executable = myExecutablePath;
        return executable != null ? Path.of(executable) : null;
    }

    @RequiredReadAction
    @Override
    public void commit(DelphiModuleExtension mutableModuleExtension) {
        super.commit(mutableModuleExtension);
        myProjectFilePath = mutableModuleExtension.getProjectFilePath();
        myMainFilePath = mutableModuleExtension.getMainFilePath();
        myConfiguration = mutableModuleExtension.getConfiguration();
        myPlatform = mutableModuleExtension.getPlatform();
        myOutputPath = mutableModuleExtension.getOutputPath();
        myExecutablePath = mutableModuleExtension.getExecutablePath();
    }

    protected boolean isModifiedImpl(DelphiModuleExtension originExtension) {
        return myIsEnabled != originExtension.isEnabled()
            || !Objects.equals(myProjectFilePath, originExtension.getProjectFilePath())
            || !Objects.equals(myMainFilePath, originExtension.getMainFilePath())
            || !Objects.equals(myConfiguration, originExtension.getConfiguration())
            || !Objects.equals(myPlatform, originExtension.getPlatform())
            || !Objects.equals(myOutputPath, originExtension.getOutputPath())
            || !Objects.equals(myExecutablePath, originExtension.getExecutablePath());
    }

    @Override
    protected void getStateImpl(Element element) {
        super.getStateImpl(element);
        if (myProjectFilePath != null) {
            element.setAttribute(ATTR_PROJECT_FILE, VirtualFileUtil.pathToUrl(myProjectFilePath));
        }
        if (myMainFilePath != null) {
            element.setAttribute(ATTR_MAIN_FILE, VirtualFileUtil.pathToUrl(myMainFilePath));
        }
        if (myConfiguration != null) {
            element.setAttribute(ATTR_CONFIGURATION, myConfiguration);
        }
        if (myPlatform != null) {
            element.setAttribute(ATTR_PLATFORM, myPlatform);
        }
        if (myOutputPath != null) {
            element.setAttribute(ATTR_OUTPUT, VirtualFileUtil.pathToUrl(myOutputPath));
        }
        if (myExecutablePath != null) {
            element.setAttribute(ATTR_EXECUTABLE, VirtualFileUtil.pathToUrl(myExecutablePath));
        }
    }

    @RequiredReadAction
    @Override
    protected void loadStateImpl(Element element) {
        super.loadStateImpl(element);
        myProjectFilePath = toPath(element.getAttributeValue(ATTR_PROJECT_FILE));
        myMainFilePath = toPath(element.getAttributeValue(ATTR_MAIN_FILE));
        myConfiguration = element.getAttributeValue(ATTR_CONFIGURATION);
        myPlatform = element.getAttributeValue(ATTR_PLATFORM);
        myOutputPath = toPath(element.getAttributeValue(ATTR_OUTPUT));
        myExecutablePath = toPath(element.getAttributeValue(ATTR_EXECUTABLE));
    }

    @Nullable
    private static String toPath(@Nullable String url) {
        return url != null ? VirtualFileUtil.urlToPath(url) : null;
    }
}
