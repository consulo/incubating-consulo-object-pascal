package com.siberika.idea.pascal.module;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: George Bakhtadze
 * Date: 03/05/2019
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class PascalProjectService {

    public static final String KEY_PARSING = "parsing";

    private static final Map<String, Object> DATA = new ConcurrentHashMap<>();

    public Object getData(String key) {
        return DATA.get(key);
    }

    public void setData(String key, Object value) {
        if (value != null) {
            DATA.put(key, value);
        }
    }

    public void remove(String key) {
        DATA.remove(key);
    }
}
