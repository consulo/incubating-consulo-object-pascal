package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.jps.util.SysUtils;
import consulo.platform.Platform;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.SourcesOrderRootType;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class FPCSdkType extends BasePascalSdkType {

    private static final Logger LOG = Logger.getInstance(FPCSdkType.class.getName());
    private static final String[] LIBRARY_DIRS = {"rtl", "rtl-objpas", "rtl-console", "pthreads", "regexpr", "x11", "windows"};

    @NotNull
    public static FPCSdkType getInstance() {
        return SdkType.EP_NAME.findExtensionOrFail(FPCSdkType.class);
    }

    public FPCSdkType() {
        super("FPCSdkType", PascalCompilerFamily.FPC);
        loadResources("fpc");
    }

    @Nonnull
    @Override
    public Collection<String> suggestHomePaths() {
        Collection<String> result = new ArrayList<>();

        List<String> paths = Arrays.asList("/usr/lib/codetyphon/fpc/fpc32", "/usr/lib/codetyphon/fpc",
                "/usr/lib/fpc", "/usr/share/fpc", "/usr/local/lib/fpc");
        if (SystemInfo.isWindows) {
            paths = Arrays.asList("c:\\codetyphon\\fpc\\fpc32", "c:\\codetyphon\\fpc", "c:\\fpc");
        }
        for (String path : paths) {
            if (new File(path).isDirectory()) {
                result.add(path);
            }
        }

        return result;
    }

    @NotNull
    @Override
    public Image getIcon() {
        return PascalIcons.GENERAL;
    }

    @Override
    public boolean isValidSdkHome(@NotNull final String path) {
        LOG.info("Checking SDK path: " + path);
        final File fpcExe = PascalSdkUtil.getFPCExecutable(path);
        return fpcExe.isFile() && fpcExe.canExecute();
    }

    @NotNull
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        String version = getVersionString(sdkHome);
        if (version == null) return "Free Pascal v. ?? at " + sdkHome;
        return "Free Pascal v. " + version + " | " + getTargetString(sdkHome);
    }

    @Nullable
    public String getVersionString(String sdkHome) {
        LOG.info("Getting version for SDK path: " + sdkHome);
        try {
            return SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getFPCExecutable(sdkHome).getAbsolutePath(), SysUtils.SHORT_TIMEOUT, PascalSdkUtil.FPC_PARAMS_VERSION_GET);
        } catch (PascalException e) {
            LOG.info("Error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            LOG.info("Error: " + e.getMessage(), e);
        }
        return null;
    }

    @Nullable
    private static String getTargetString(String sdkHome) {
        LOG.info("Getting target for SDK path: " + sdkHome);
        try {
            return SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getFPCExecutable(sdkHome).getAbsolutePath(), SysUtils.SHORT_TIMEOUT, PascalSdkUtil.FPC_PARAMS_TARGET_GET);
        } catch (PascalException e) {
            LOG.info("Error: " + e.getMessage(), e);
        }
        return null;
    }

    @NotNull
    @NonNls
    @Override
    public String getPresentableName() {
        return "Free Pascal SDK";
    }

    @Override
    public void setupSdkPaths(@NotNull final Sdk sdk) {
        String target = getTargetString(sdk.getHomePath());
        configureSdkPaths(sdk, target);
        configureOptions(sdk, getAdditionalData(sdk), target);
    }

    @Override
    protected void configureOptions(@NotNull Sdk sdk, PascalSdkData data, String target) {
        super.configureOptions(sdk, data, target);
        File file = PascalSdkUtil.getPPUDumpExecutable(sdk.getHomePath() != null ? sdk.getHomePath() : "");
        data.setValue(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey(), file.getAbsolutePath());
        StringBuilder sb = new StringBuilder("-Sa -gl -O3 ");
        Platform.OperatingSystem operatingSystem = Platform.current().os();
        if (operatingSystem.isWindows()) {
            sb.append("-dMSWINDOWS ");
        } else {
            sb.append("-dPOSIX ");
            if (operatingSystem.isMac()) {
                sb.append("-dMACOS ");
            } else {
                sb.append("-dLINUX ");
            }
        }
        if (target.contains("_64")) {
            sb.append("-dCPUX64 ");
        } else {
            sb.append("-dCPUX86 ");
        }
        data.setValue(PascalSdkData.Keys.COMPILER_OPTIONS.getKey(), sb.toString());
        data.setValue(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG.getKey(), "-Ddebug -gw -gh -CroiO -godwarfsets -Sa");
    }

    private static void configureSdkPaths(@NotNull final Sdk sdk, String target) {
        LOG.info("Setting up SDK paths for SDK at " + sdk.getHomePath());
        final SdkModificator sdkModificator = sdk.getSdkModificator();

        URL builtins = FPCSdkType.class.getResource("/builtins.pas");
        if(builtins != null) {
            String url = VfsUtil.convertFromUrl(builtins);
            // java plugin can be not installed
            url = url.replace("jar://", "zip://");
            
            VirtualFile builtinFile = VirtualFileManager.getInstance().findFileByUrl(url);
            if(builtinFile != null) {
                sdkModificator.addRoot(builtinFile, BinariesOrderRootType.getInstance());
                sdkModificator.addRoot(builtinFile, SourcesOrderRootType.getInstance());
            }
        }

        if (target != null) {
            target = target.replace(' ', '-');
            for (String dir : LIBRARY_DIRS) {
                VirtualFile vdir = getLibrary(sdk, target, dir);
                if (vdir != null) {
                    sdkModificator.addRoot(vdir, OrderRootType.CLASSES);
                }
            }
        }
        sdkModificator.commitChanges();
    }

    private static VirtualFile getLibrary(Sdk sdk, String target, String name) {
        File rtlDir = new File(sdk.getHomePath() + File.separatorChar + "units" + File.separatorChar + target + File.separatorChar + name);
        if (!rtlDir.exists()) {
            rtlDir = new File(sdk.getHomePath() + File.separatorChar + sdk.getVersionString() + File.separatorChar + "units" + File.separatorChar + target + File.separatorChar + name);
        }
        return LocalFileSystem.getInstance().findFileByIoFile(rtlDir);
    }

    @Override
    public boolean isRootTypeApplicable(@NotNull OrderRootType type) {
        return type.equals(OrderRootType.SOURCES) || type.equals(OrderRootType.CLASSES);
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
        return new PascalSdkConfigUI();
    }

    public static void applyDebugUnitFile(Sdk sdk, String debugUnitDir, String debugUnitName) {
        if ((sdk != null) && (sdk.getSdkType() instanceof FPCSdkType)) {
            PascalSdkData data = BasePascalSdkType.getAdditionalData(sdk);
            data.setValue(PascalSdkData.Keys.COMPILER_IMPLICIT_UNITS_DIR.getKey(), debugUnitDir);
            data.setValue(PascalSdkData.Keys.COMPILER_IMPLICIT_UNITS.getKey(), debugUnitName);
        }
    }

    private static void addString(PascalSdkData data, PascalSdkData.Keys key, String str, String delimiter) {
        String value = (String) data.getValue(key.getKey());
        if (StringUtil.isEmpty(value)) {
            data.setValue(key.getKey(), str);
        } else {
            Set<String> valueSet = new SmartHashSet<>(Arrays.asList(value.split(delimiter)));
            valueSet.add(str);
            data.setValue(key.getKey(), String.join(delimiter, valueSet));
        }
    }

}
