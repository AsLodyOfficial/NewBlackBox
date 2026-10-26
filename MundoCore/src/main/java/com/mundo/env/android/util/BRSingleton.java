package com.mundo.env.android.util;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRSingleton {
  public static SingletonStatic getWithException() {
    return BlackReflection.create(SingletonStatic.class, null, true);
  }

  public static SingletonStatic get() {
    return BlackReflection.create(SingletonStatic.class, null, false);
  }

  public static SingletonContext getWithException(final Object caller) {
    return BlackReflection.create(SingletonContext.class, caller, true);
  }

  public static SingletonContext get(final Object caller) {
    return BlackReflection.create(SingletonContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(SingletonContext.class);
  }
}
