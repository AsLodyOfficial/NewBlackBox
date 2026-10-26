package com.mundo.env.android.os;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRServiceManager {
  public static ServiceManagerStatic getWithException() {
    return BlackReflection.create(ServiceManagerStatic.class, null, true);
  }

  public static ServiceManagerStatic get() {
    return BlackReflection.create(ServiceManagerStatic.class, null, false);
  }

  public static ServiceManagerContext getWithException(final Object caller) {
    return BlackReflection.create(ServiceManagerContext.class, caller, true);
  }

  public static ServiceManagerContext get(final Object caller) {
    return BlackReflection.create(ServiceManagerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ServiceManagerContext.class);
  }
}
