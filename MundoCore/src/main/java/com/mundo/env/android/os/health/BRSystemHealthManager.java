package com.mundo.env.android.os.health;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRSystemHealthManager {
  public static SystemHealthManagerStatic getWithException() {
    return BlackReflection.create(SystemHealthManagerStatic.class, null, true);
  }

  public static SystemHealthManagerStatic get() {
    return BlackReflection.create(SystemHealthManagerStatic.class, null, false);
  }

  public static SystemHealthManagerContext getWithException(final Object caller) {
    return BlackReflection.create(SystemHealthManagerContext.class, caller, true);
  }

  public static SystemHealthManagerContext get(final Object caller) {
    return BlackReflection.create(SystemHealthManagerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(SystemHealthManagerContext.class);
  }
}
