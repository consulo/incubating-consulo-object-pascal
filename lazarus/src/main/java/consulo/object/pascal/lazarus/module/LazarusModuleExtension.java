package consulo.object.pascal.lazarus.module;

import consulo.annotation.access.RequiredReadAction;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.object.pascal.lazarus.LazarusBuild;
import consulo.object.pascal.module.extension.BaseObjectPascalModuleExtension;
import consulo.object.pascal.module.extension.PascalModuleBuild;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nullable;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusModuleExtension extends BaseObjectPascalModuleExtension<LazarusModuleExtension> {
    public static final String ID = "lazarus";

    private static final String PACKAGE_EXTENSION = ".lpk";
    private static final String ATTR_PROJECT_FILE = "project-file";
    private static final String ATTR_BUILD_MODE = "build-mode";
    private static final String ATTR_TARGET_FILE = "target-file";

    @Nullable
    protected String myProjectFilePath;
    @Nullable
    protected String myBuildMode;
    @Nullable
    protected String myTargetFilePath;

    public LazarusModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @Nullable
    public String getProjectFilePath() {
        return myProjectFilePath;
    }

    @Nullable
    public String getBuildMode() {
        return myBuildMode;
    }

    @Nullable
    public String getTargetFilePath() {
        return myTargetFilePath;
    }

    public boolean isPackage() {
        String path = myProjectFilePath;
        return path != null && path.toLowerCase(Locale.ROOT).endsWith(PACKAGE_EXTENSION);
    }

    @Nullable
    @RequiredReadAction
    @Override
    public PascalModuleBuild createBuild() {
        return LazarusBuild.create(this);
    }

    @Nullable
    @RequiredReadAction
    @Override
    public Path getExecutable() {
        String target = myTargetFilePath;
        return target != null && !isPackage() ? Path.of(target) : null;
    }

    @RequiredReadAction
    @Override
    public void commit(LazarusModuleExtension mutableModuleExtension) {
        super.commit(mutableModuleExtension);
        myProjectFilePath = mutableModuleExtension.getProjectFilePath();
        myBuildMode = mutableModuleExtension.getBuildMode();
        myTargetFilePath = mutableModuleExtension.getTargetFilePath();
    }

    @Override
    protected boolean isModifiedImpl(LazarusModuleExtension originExtension) {
        return super.isModifiedImpl(originExtension)
            || !Objects.equals(myProjectFilePath, originExtension.getProjectFilePath())
            || !Objects.equals(myBuildMode, originExtension.getBuildMode())
            || !Objects.equals(myTargetFilePath, originExtension.getTargetFilePath());
    }

    @Override
    protected void getStateImpl(Element element) {
        super.getStateImpl(element);
        if (myProjectFilePath != null) {
            element.setAttribute(ATTR_PROJECT_FILE, VirtualFileUtil.pathToUrl(myProjectFilePath));
        }
        if (myBuildMode != null) {
            element.setAttribute(ATTR_BUILD_MODE, myBuildMode);
        }
        if (myTargetFilePath != null) {
            element.setAttribute(ATTR_TARGET_FILE, VirtualFileUtil.pathToUrl(myTargetFilePath));
        }
    }

    @RequiredReadAction
    @Override
    protected void loadStateImpl(Element element) {
        super.loadStateImpl(element);
        myProjectFilePath = toPath(element.getAttributeValue(ATTR_PROJECT_FILE));
        myBuildMode = element.getAttributeValue(ATTR_BUILD_MODE);
        myTargetFilePath = toPath(element.getAttributeValue(ATTR_TARGET_FILE));
    }

    @Nullable
    private static String toPath(@Nullable String url) {
        return url != null ? VirtualFileUtil.urlToPath(url) : null;
    }
}
