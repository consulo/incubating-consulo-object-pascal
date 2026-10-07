package com.siberika.idea.pascal.sdk;

import consulo.virtualFileSystem.VirtualFile;

/**
 * Author: George Bakhtadze
 * Date: 06/09/2016
 */
public class Define {
    public final String name;
    public final VirtualFile virtualFile;
    public final int offset;
    public final String value;

    public Define(String name, VirtualFile virtualFile, int offset) {
        this(name, virtualFile, offset, null);
    }

    public Define(String name, VirtualFile virtualFile, int offset, String value) {
        this.name = name;
        this.virtualFile = virtualFile;
        this.offset = offset;
        this.value = value;
    }
}
