package com.siberika.idea.pascal.jps;

import consulo.annotation.internal.MigratedExtensionsTo;
import consulo.application.CommonBundle;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Author: George Bakhtadze
 * Date: 1/5/13
 */
@Deprecated
@MigratedExtensionsTo(ObjectPascalLocalize.class)
public class JpsPascalBundle {

  private static Reference<ResourceBundle> ourBundle;

  @NonNls
  public static final String JPSBUNDLE = "JpsPascalBundle";

  public static String message(@PropertyKey(resourceBundle = JPSBUNDLE)String key, Object... params) {
    return CommonBundle.message(getJpsbundle(), key, params);
  }

  private static ResourceBundle getJpsbundle() {
    ResourceBundle bundle = null;

    if (ourBundle != null) bundle = ourBundle.get();

    if (bundle == null) {
      bundle = ResourceBundle.getBundle(JPSBUNDLE);
      ourBundle = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }
}
