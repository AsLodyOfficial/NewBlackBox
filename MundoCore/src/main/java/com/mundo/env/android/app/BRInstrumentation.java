package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRInstrumentation {
  public static InstrumentationStatic getWithException() {
    return BlackReflection.create(InstrumentationStatic.class, null, true);
  }

  public static InstrumentationStatic get() {
    return BlackReflection.create(InstrumentationStatic.class, null, false);
  }

  public static InstrumentationContext getWithException(final Object caller) {
    return BlackReflection.create(InstrumentationContext.class, caller, true);
  }

  public static InstrumentationContext get(final Object caller) {
    return BlackReflection.create(InstrumentationContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(InstrumentationContext.class);
  }
}
