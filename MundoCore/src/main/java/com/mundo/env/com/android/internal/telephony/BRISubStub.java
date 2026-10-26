package com.mundo.env.com.android.internal.telephony;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRISubStub {
  public static ISubStubStatic getWithException() {
    return BlackReflection.create(ISubStubStatic.class, null, true);
  }

  public static ISubStubStatic get() {
    return BlackReflection.create(ISubStubStatic.class, null, false);
  }

  public static ISubStubContext getWithException(final Object caller) {
    return BlackReflection.create(ISubStubContext.class, caller, true);
  }

  public static ISubStubContext get(final Object caller) {
    return BlackReflection.create(ISubStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ISubStubContext.class);
  }
}
