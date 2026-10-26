package com.mundo.env.android.accounts;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIAccountManagerStub {
  public static IAccountManagerStubStatic getWithException() {
    return BlackReflection.create(IAccountManagerStubStatic.class, null, true);
  }

  public static IAccountManagerStubStatic get() {
    return BlackReflection.create(IAccountManagerStubStatic.class, null, false);
  }

  public static IAccountManagerStubContext getWithException(final Object caller) {
    return BlackReflection.create(IAccountManagerStubContext.class, caller, true);
  }

  public static IAccountManagerStubContext get(final Object caller) {
    return BlackReflection.create(IAccountManagerStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IAccountManagerStubContext.class);
  }
}
