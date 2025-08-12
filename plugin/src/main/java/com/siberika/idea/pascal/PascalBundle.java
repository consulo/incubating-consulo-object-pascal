package com.siberika.idea.pascal;

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
public class PascalBundle {

  private static Reference<ResourceBundle> ourBundle;

  @NonNls
  public static final String BUNDLE = "PascalBundle";

  public static String message(@PropertyKey(resourceBundle = BUNDLE)String key, Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;

    if (ourBundle != null) bundle = ourBundle.get();

    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }
}
