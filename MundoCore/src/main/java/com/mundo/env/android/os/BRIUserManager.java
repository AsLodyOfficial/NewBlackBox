package com.mundo.env.android.os;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIUserManager {
  public static IUserManagerStatic getWithException() {
    return BlackReflection.create(IUserManagerStatic.class, null, true);
  }

  public static IUserManagerStatic get() {
    return BlackReflection.create(IUserManagerStatic.class, null, false);
  }

  public static IUserManagerContext getWithException(final Object caller) {
    return BlackReflection.create(IUserManagerContext.class, caller, true);
  }

  public static IUserManagerContext get(final Object caller) {
    return BlackReflection.create(IUserManagerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IUserManagerContext.class);
  }
}
