package com.mundo.env.com.android.internal;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRRstyleable {
  public static RstyleableStatic getWithException() {
    return BlackReflection.create(RstyleableStatic.class, null, true);
  }

  public static RstyleableStatic get() {
    return BlackReflection.create(RstyleableStatic.class, null, false);
  }

  public static RstyleableContext getWithException(final Object caller) {
    return BlackReflection.create(RstyleableContext.class, caller, true);
  }

  public static RstyleableContext get(final Object caller) {
    return BlackReflection.create(RstyleableContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(RstyleableContext.class);
  }
}
