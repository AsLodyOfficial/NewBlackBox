package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIApplicationThread {
  public static IApplicationThreadStatic getWithException() {
    return BlackReflection.create(IApplicationThreadStatic.class, null, true);
  }

  public static IApplicationThreadStatic get() {
    return BlackReflection.create(IApplicationThreadStatic.class, null, false);
  }

  public static IApplicationThreadContext getWithException(final Object caller) {
    return BlackReflection.create(IApplicationThreadContext.class, caller, true);
  }

  public static IApplicationThreadContext get(final Object caller) {
    return BlackReflection.create(IApplicationThreadContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IApplicationThreadContext.class);
  }
}
