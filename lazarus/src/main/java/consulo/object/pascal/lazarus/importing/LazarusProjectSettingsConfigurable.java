package consulo.object.pascal.lazarus.importing;

import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.object.pascal.lazarus.LazarusFileReader;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;
import consulo.object.pascal.lazarus.setting.LazarusProjectSettings;
import consulo.object.pascal.sdk.PascalModuleSdkUtil;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class LazarusProjectSettingsConfigurable extends AbstractExternalProjectSettingsConfigurable<LazarusProjectSettings> {
    private static final Logger LOG = Logger.getInstance(LazarusProjectSettingsConfigurable.class);

    private static final String DEFAULT_BUILD_MODE = "";

    @Nullable
    private MutableFlatDataModel<String> myBuildModes;
    @Nullable
    private ComboBox<String> myBuildModeBox;
    @Nullable
    private BundleBox mySdkBox;
    @Nullable
    private String myInitialSdkName;

    public LazarusProjectSettingsConfigurable(LazarusProjectSettings settings, ExternalSystemSettingsPlace place) {
        super(settings, place);
    }

    @RequiredUIAccess
    @Override
    protected Component createExtraUIComponent(Disposable uiDisposable) {
        LazarusProjectSettings settings = getSettings();

        MutableFlatDataModel<String> buildModes = FlatDataModel.of(List.of(DEFAULT_BUILD_MODE));
        ComboBox<String> buildModeBox = ComboBox.create(buildModes);
        buildModeBox.setTextRenderer(item -> item == null || item.isEmpty() ? LazarusLocalize.extensionBuildModeDefault() : LocalizeValue.of(item));
        myBuildModes = buildModes;
        myBuildModeBox = buildModeBox;
        fillBuildModes(settings.getExternalProjectPath());

        BundleBox sdkBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilter(sdkType -> sdkType instanceof BasePascalSdkType)
            .build();
        mySdkBox = sdkBox;
        String sdkName = settings.getSdkName();
        if (sdkName == null) {
            Sdk sdk = PascalModuleSdkUtil.findDefaultSdk(FPCSdkType.getInstance());
            sdkName = sdk != null ? sdk.getName() : null;
        }
        if (sdkName != null) {
            sdkBox.setSelectedBundle(sdkName);
        }
        myInitialSdkName = sdkBox.getSelectedBundleName();

        return FormBuilder.create()
            .addLabeled(LazarusLocalize.extensionBuildModeLabel(), buildModeBox)
            .addLabeled(LazarusLocalize.importSdkLabel(), sdkBox.getComponent())
            .build();
    }

    @RequiredUIAccess
    @Override
    public void onLinkedProjectPathChange(String path) {
        fillBuildModes(path);
    }

    @RequiredUIAccess
    private void fillBuildModes(@Nullable String projectPath) {
        MutableFlatDataModel<String> buildModes = myBuildModes;
        ComboBox<String> buildModeBox = myBuildModeBox;
        if (buildModes == null || buildModeBox == null) {
            return;
        }
        buildModes.removeAll();
        buildModes.add(DEFAULT_BUILD_MODE);
        if (projectPath != null) {
            try {
                for (String buildMode : LazarusFileReader.readProject(Path.of(projectPath)).buildModes()) {
                    buildModes.add(buildMode);
                }
            }
            catch (IOException | InvalidPathException e) {
                LOG.warn("Cannot read Lazarus build modes from " + projectPath, e);
            }
        }
        String current = getSettings().getBuildMode();
        if (current != null && !contains(buildModes, current)) {
            buildModes.add(current);
        }
        buildModeBox.setValue(current != null ? current : DEFAULT_BUILD_MODE);
    }

    private static boolean contains(FlatDataModel<String> model, String value) {
        for (int i = 0; i < model.getSize(); i++) {
            if (value.equals(model.get(i))) {
                return true;
            }
        }
        return false;
    }

    @RequiredUIAccess
    @Override
    protected boolean isExtraModified() {
        return !Objects.equals(selectedBuildMode(), getSettings().getBuildMode())
            || !Objects.equals(selectedSdkName(), myInitialSdkName);
    }

    @RequiredUIAccess
    @Override
    protected void applyExtra() {
        LazarusProjectSettings settings = getSettings();
        settings.setBuildMode(selectedBuildMode());
        settings.setSdkName(selectedSdkName());
        myInitialSdkName = selectedSdkName();
    }

    @Nullable
    @RequiredUIAccess
    private String selectedBuildMode() {
        ComboBox<String> buildModeBox = myBuildModeBox;
        String value = buildModeBox != null ? buildModeBox.getValue() : null;
        return value == null || value.isEmpty() ? null : value;
    }

    @Nullable
    @RequiredUIAccess
    private String selectedSdkName() {
        BundleBox sdkBox = mySdkBox;
        return sdkBox != null ? sdkBox.getSelectedBundleName() : null;
    }
}
