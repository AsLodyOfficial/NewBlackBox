package com.mundo.env.android.os;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIVibratorManagerServiceStub {
  public static IVibratorManagerServiceStubStatic getWithException() {
    return BlackReflection.create(IVibratorManagerServiceStubStatic.class, null, true);
  }

  public static IVibratorManagerServiceStubStatic get() {
    return BlackReflection.create(IVibratorManagerServiceStubStatic.class, null, false);
  }

  public static IVibratorManagerServiceStubContext getWithException(final Object caller) {
    return BlackReflection.create(IVibratorManagerServiceStubContext.class, caller, true);
  }

  public static IVibratorManagerServiceStubContext get(final Object caller) {
    return BlackReflection.create(IVibratorManagerServiceStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IVibratorManagerServiceStubContext.class);
  }
}
