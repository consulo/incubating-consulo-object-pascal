package com.siberika.idea.pascal;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.util.io.StreamUtil;
import com.siberika.idea.pascal.sdk.FPCSdkType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Author: George Bakhtadze
 * Date: 30/11/2015
 */
public class PascalAppService  {

    private static final Logger LOG = Logger.getInstance(PascalAppService.class);

    public static final String PASCAL_NOTIFICATION_GROUP = "I-Pascal";

    private File debugUnitFile;
    private File debugUnitDir;

    public PascalAppService() {
        new NotificationGroup(PASCAL_NOTIFICATION_GROUP, NotificationDisplayType.BALLOON, true);
        initDebugUnit();
    }

    public File getDebugUnitDir() {
        return debugUnitDir;
    }

    public String getDebugUnitName() {
        return "ipasdbg";
    }

    synchronized private void initDebugUnit() {
        if (debugUnitFile != null) {
            return;
        }
        try {
            debugUnitDir = Files.createTempDirectory("ipas").toFile();
            debugUnitFile = new File(debugUnitDir, getDebugUnitName() + ".pas");
            debugUnitDir.deleteOnExit();
            debugUnitFile.deleteOnExit();
            LOG.debug("Debug unit file: " + debugUnitFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.info("ERROR: failed to create debug unit temp file: " + e.getMessage());
        }
        try (InputStream data = PascalAppService.class.getResourceAsStream("/ipasdbg.pas");
             FileOutputStream os = new FileOutputStream(debugUnitFile)) {
             StreamUtil.copyStreamContent(data, os);
        } catch (IOException e) {
            LOG.info("ERROR: failed to prepare debug unit file: " + e.getMessage());
        }
        Sdk[] sdks = SdkTable.getInstance().getAllSdks();
        for (Sdk sdk : sdks) {
            FPCSdkType.applyDebugUnitFile(sdk, debugUnitDir.getAbsolutePath(), getDebugUnitName());
        }
    }
}
