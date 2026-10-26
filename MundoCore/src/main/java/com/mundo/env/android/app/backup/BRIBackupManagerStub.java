package com.mundo.env.android.app.backup;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIBackupManagerStub {
  public static IBackupManagerStubStatic getWithException() {
    return BlackReflection.create(IBackupManagerStubStatic.class, null, true);
  }

  public static IBackupManagerStubStatic get() {
    return BlackReflection.create(IBackupManagerStubStatic.class, null, false);
  }

  public static IBackupManagerStubContext getWithException(final Object caller) {
    return BlackReflection.create(IBackupManagerStubContext.class, caller, true);
  }

  public static IBackupManagerStubContext get(final Object caller) {
    return BlackReflection.create(IBackupManagerStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IBackupManagerStubContext.class);
  }
}
