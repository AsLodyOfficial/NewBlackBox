package com.mundo.env.android.os;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRBaseBundle {
  public static BaseBundleStatic getWithException() {
    return BlackReflection.create(BaseBundleStatic.class, null, true);
  }

  public static BaseBundleStatic get() {
    return BlackReflection.create(BaseBundleStatic.class, null, false);
  }

  public static BaseBundleContext getWithException(final Object caller) {
    return BlackReflection.create(BaseBundleContext.class, caller, true);
  }

  public static BaseBundleContext get(final Object caller) {
    return BlackReflection.create(BaseBundleContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(BaseBundleContext.class);
  }
}
