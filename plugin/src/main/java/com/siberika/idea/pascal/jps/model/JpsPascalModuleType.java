package com.siberika.idea.pascal.jps.model;

import consulo.util.dataholder.Key;

public class JpsPascalModuleType {
    public static final JpsPascalModuleType INSTANCE = new JpsPascalModuleType();

    public static final String MODULE_TYPE_ID = "PASCAL_MODULE";
    public static final Key<Object> USERDATA_KEY_MAIN_FILE = new Key<Object>("mainFile");
    public static final Key<Object> USERDATA_KEY_EXE_OUTPUT_PATH = new Key<Object>("exeOutputPath");

    private JpsPascalModuleType() {
    }
}