package com.siberika.idea.pascal;

import consulo.annotation.DeprecationInfo;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;

@Deprecated
@DeprecationInfo("Use ObjectPascalIconGroup")
public interface PascalIcons {
    Image GENERAL = ObjectPascalIconGroup.pascal_16x16();
    Image MODULE = GENERAL;
    Image UNIT = ObjectPascalIconGroup.unit();
    Image PROGRAM = ObjectPascalIconGroup.program();
    Image INCLUDE = Image.empty(Image.DEFAULT_ICON_SIZE); // TODO [VISTALL] missed in original repo
    Image COMPILED = ObjectPascalIconGroup.compiled();

    Image FILE_PROGRAM = ObjectPascalIconGroup.program();
    Image FILE_LIBRARY = ObjectPascalIconGroup.library();
    Image FILE_INCLUDE = Image.empty(Image.DEFAULT_ICON_SIZE); // TODO [VISTALL] missed in original repo

    Image TYPE = ObjectPascalIconGroup.ntype();
    Image VARIABLE = ObjectPascalIconGroup.nvar();
    Image CONSTANT = ObjectPascalIconGroup.nconst();
    Image PROPERTY = ObjectPascalIconGroup.nproperty();
    Image ROUTINE = ObjectPascalIconGroup.nroutine();
    Image INTERFACE = ObjectPascalIconGroup.ninterface();
    Image CLASS = ObjectPascalIconGroup.nclass();
    Image OBJECT = ObjectPascalIconGroup.nobject();
    Image RECORD = ObjectPascalIconGroup.nrecord();
    Image HELPER = ObjectPascalIconGroup.nhelper();

    final class Idea {
        public static final Image RUN = PlatformIconGroup.actionsExecute();
        public static final Image USED_BY = PlatformIconGroup.gutterImplementingmethod();
    }
}
