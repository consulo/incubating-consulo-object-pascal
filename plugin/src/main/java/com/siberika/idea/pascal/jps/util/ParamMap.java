package com.siberika.idea.pascal.jps.util;

import com.intellij.openapi.diagnostic.Logger;

import java.util.HashMap;

/**
 * Author: George Bakhtadze
 * Date: 11/05/2014
 */
public class ParamMap extends HashMap<String, String> {
    private static final Logger LOG = Logger.getInstance(ParamMap.class.getName());
    public ParamMap() {
    }

    public ParamMap(ParamMap msg) {
        this.putAll(msg);
    }

    public ParamMap addPair(String key, String value) {
        this.put(key, value);
        return this;
    }
}

