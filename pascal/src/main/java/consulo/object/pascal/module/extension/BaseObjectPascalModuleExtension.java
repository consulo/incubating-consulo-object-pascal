package consulo.object.pascal.module.extension;

import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import consulo.annotation.access.RequiredReadAction;
import consulo.content.bundle.SdkType;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionWithSdkBase;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nullable;
import org.jdom.Element;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public abstract class BaseObjectPascalModuleExtension<S extends BaseObjectPascalModuleExtension<S>> extends ModuleExtensionWithSdkBase<S>
    implements ObjectPascalModuleExtension<S> {
    private static final String ATTR_MAIN_FILE = "main-file";

    @Nullable
    protected String myMainFilePath;

    protected BaseObjectPascalModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    @Nullable
    @Override
    public String getMainFilePath() {
        return myMainFilePath;
    }

    @Nullable
    @Override
    public String getOutputPath() {
        return null;
    }

    @Override
    public Class<? extends SdkType> getSdkTypeClass() {
        return BasePascalSdkType.class;
    }

    @RequiredReadAction
    @Override
    public void commit(S mutableModuleExtension) {
        super.commit(mutableModuleExtension);
        myMainFilePath = mutableModuleExtension.getMainFilePath();
    }

    protected boolean isModifiedImpl(S originExtension) {
        return super.isModifiedImpl(originExtension) || !Objects.equals(myMainFilePath, originExtension.getMainFilePath());
    }

    @Override
    protected void getStateImpl(Element element) {
        super.getStateImpl(element);
        if (myMainFilePath != null) {
            element.setAttribute(ATTR_MAIN_FILE, VirtualFileUtil.pathToUrl(myMainFilePath));
        }
    }

    @RequiredReadAction
    @Override
    protected void loadStateImpl(Element element) {
        super.loadStateImpl(element);
        String url = element.getAttributeValue(ATTR_MAIN_FILE);
        myMainFilePath = url != null ? VirtualFileUtil.urlToPath(url) : null;
    }
}
