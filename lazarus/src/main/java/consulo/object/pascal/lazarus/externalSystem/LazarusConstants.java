package consulo.object.pascal.lazarus.externalSystem;

import consulo.externalSystem.model.ProjectSystemId;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.lazarus.localize.LazarusLocalize;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public interface LazarusConstants {
    ProjectSystemId SYSTEM_ID = new ProjectSystemId("LAZARUS", LazarusLocalize.projectSystemName(), ObjectPascalIconGroup.lazarus());

    String PROJECT_EXTENSION = "lpi";

    String PACKAGE_EXTENSION = "lpk";
}
