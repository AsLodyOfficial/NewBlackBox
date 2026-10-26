package com.mundo.env.android.content.integrity;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIAppIntegrityManagerStub {
  public static IAppIntegrityManagerStubStatic getWithException() {
    return BlackReflection.create(IAppIntegrityManagerStubStatic.class, null, true);
  }

  public static IAppIntegrityManagerStubStatic get() {
    return BlackReflection.create(IAppIntegrityManagerStubStatic.class, null, false);
  }

  public static IAppIntegrityManagerStubContext getWithException(final Object caller) {
    return BlackReflection.create(IAppIntegrityManagerStubContext.class, caller, true);
  }

  public static IAppIntegrityManagerStubContext get(final Object caller) {
    return BlackReflection.create(IAppIntegrityManagerStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IAppIntegrityManagerStubContext.class);
  }
}
