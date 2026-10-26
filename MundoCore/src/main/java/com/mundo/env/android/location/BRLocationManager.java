package com.mundo.env.android.location;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRLocationManager {
  public static LocationManagerStatic getWithException() {
    return BlackReflection.create(LocationManagerStatic.class, null, true);
  }

  public static LocationManagerStatic get() {
    return BlackReflection.create(LocationManagerStatic.class, null, false);
  }

  public static LocationManagerContext getWithException(final Object caller) {
    return BlackReflection.create(LocationManagerContext.class, caller, true);
  }

  public static LocationManagerContext get(final Object caller) {
    return BlackReflection.create(LocationManagerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(LocationManagerContext.class);
  }
}
