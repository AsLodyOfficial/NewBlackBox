package com.mundo.env.com.android.internal.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRISmtOpsService {
  public static ISmtOpsServiceStatic getWithException() {
    return BlackReflection.create(ISmtOpsServiceStatic.class, null, true);
  }

  public static ISmtOpsServiceStatic get() {
    return BlackReflection.create(ISmtOpsServiceStatic.class, null, false);
  }

  public static ISmtOpsServiceContext getWithException(final Object caller) {
    return BlackReflection.create(ISmtOpsServiceContext.class, caller, true);
  }

  public static ISmtOpsServiceContext get(final Object caller) {
    return BlackReflection.create(ISmtOpsServiceContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ISmtOpsServiceContext.class);
  }
}
