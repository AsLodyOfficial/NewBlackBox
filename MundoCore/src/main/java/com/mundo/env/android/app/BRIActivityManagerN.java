package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIActivityManagerN {
  public static IActivityManagerNStatic getWithException() {
    return BlackReflection.create(IActivityManagerNStatic.class, null, true);
  }

  public static IActivityManagerNStatic get() {
    return BlackReflection.create(IActivityManagerNStatic.class, null, false);
  }

  public static IActivityManagerNContext getWithException(final Object caller) {
    return BlackReflection.create(IActivityManagerNContext.class, caller, true);
  }

  public static IActivityManagerNContext get(final Object caller) {
    return BlackReflection.create(IActivityManagerNContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IActivityManagerNContext.class);
  }
}
