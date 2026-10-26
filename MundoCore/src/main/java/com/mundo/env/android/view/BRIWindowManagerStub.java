package com.mundo.env.android.view;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIWindowManagerStub {
  public static IWindowManagerStubStatic getWithException() {
    return BlackReflection.create(IWindowManagerStubStatic.class, null, true);
  }

  public static IWindowManagerStubStatic get() {
    return BlackReflection.create(IWindowManagerStubStatic.class, null, false);
  }

  public static IWindowManagerStubContext getWithException(final Object caller) {
    return BlackReflection.create(IWindowManagerStubContext.class, caller, true);
  }

  public static IWindowManagerStubContext get(final Object caller) {
    return BlackReflection.create(IWindowManagerStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IWindowManagerStubContext.class);
  }
}
