package com.siberika.idea.pascal;

import consulo.annotation.DeprecationInfo;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;

@Deprecated
@DeprecationInfo("Use ObjectPascalIconGroup")
public interface PascalIcons {
    Image GENERAL = ObjectPascalIconGroup.pascal();
    Image UNIT = ObjectPascalIconGroup.unit();
    Image PROGRAM = ObjectPascalIconGroup.program();
    Image INCLUDE = ObjectPascalIconGroup.include();

    Image FILE_PROGRAM = ObjectPascalIconGroup.program();
    Image FILE_LIBRARY = ObjectPascalIconGroup.library();
    Image FILE_INCLUDE = ObjectPascalIconGroup.include();

    Image TYPE = PlatformIconGroup.nodesType();
    Image VARIABLE = PlatformIconGroup.nodesVariable();
    Image CONSTANT = PlatformIconGroup.nodesConstant();
    Image PROPERTY = PlatformIconGroup.nodesProperty();
    Image ROUTINE = PlatformIconGroup.nodesFunction();
    Image INTERFACE = PlatformIconGroup.nodesInterface();
    Image CLASS = PlatformIconGroup.nodesClass();
    Image OBJECT = PlatformIconGroup.nodesAnonymousclass();
    Image RECORD = PlatformIconGroup.nodesRecord();
    Image HELPER = ObjectPascalIconGroup.helper();

    final class Idea {
        public static final Image RUN = PlatformIconGroup.actionsExecute();
        public static final Image USED_BY = PlatformIconGroup.gutterImplementingmethod();
    }
}
