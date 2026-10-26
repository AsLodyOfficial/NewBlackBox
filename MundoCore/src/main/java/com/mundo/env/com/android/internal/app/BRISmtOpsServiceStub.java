package com.mundo.env.com.android.internal.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRISmtOpsServiceStub {
  public static ISmtOpsServiceStubStatic getWithException() {
    return BlackReflection.create(ISmtOpsServiceStubStatic.class, null, true);
  }

  public static ISmtOpsServiceStubStatic get() {
    return BlackReflection.create(ISmtOpsServiceStubStatic.class, null, false);
  }

  public static ISmtOpsServiceStubContext getWithException(final Object caller) {
    return BlackReflection.create(ISmtOpsServiceStubContext.class, caller, true);
  }

  public static ISmtOpsServiceStubContext get(final Object caller) {
    return BlackReflection.create(ISmtOpsServiceStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ISmtOpsServiceStubContext.class);
  }
}
