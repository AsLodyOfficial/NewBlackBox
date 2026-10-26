package com.mundo.env.android.os;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRHandler {
  public static HandlerStatic getWithException() {
    return BlackReflection.create(HandlerStatic.class, null, true);
  }

  public static HandlerStatic get() {
    return BlackReflection.create(HandlerStatic.class, null, false);
  }

  public static HandlerContext getWithException(final Object caller) {
    return BlackReflection.create(HandlerContext.class, caller, true);
  }

  public static HandlerContext get(final Object caller) {
    return BlackReflection.create(HandlerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(HandlerContext.class);
  }
}
