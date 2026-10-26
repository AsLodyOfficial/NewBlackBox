package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRActivityClientActivityClientControllerSingleton {
  public static ActivityClientActivityClientControllerSingletonStatic getWithException() {
    return BlackReflection.create(ActivityClientActivityClientControllerSingletonStatic.class, null, true);
  }

  public static ActivityClientActivityClientControllerSingletonStatic get() {
    return BlackReflection.create(ActivityClientActivityClientControllerSingletonStatic.class, null, false);
  }

  public static ActivityClientActivityClientControllerSingletonContext getWithException(final Object caller) {
    return BlackReflection.create(ActivityClientActivityClientControllerSingletonContext.class, caller, true);
  }

  public static ActivityClientActivityClientControllerSingletonContext get(final Object caller) {
    return BlackReflection.create(ActivityClientActivityClientControllerSingletonContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ActivityClientActivityClientControllerSingletonContext.class);
  }
}
