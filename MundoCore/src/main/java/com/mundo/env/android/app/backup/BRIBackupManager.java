package com.mundo.env.android.app.backup;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIBackupManager {
  public static IBackupManagerStatic getWithException() {
    return BlackReflection.create(IBackupManagerStatic.class, null, true);
  }

  public static IBackupManagerStatic get() {
    return BlackReflection.create(IBackupManagerStatic.class, null, false);
  }

  public static IBackupManagerContext getWithException(final Object caller) {
    return BlackReflection.create(IBackupManagerContext.class, caller, true);
  }

  public static IBackupManagerContext get(final Object caller) {
    return BlackReflection.create(IBackupManagerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IBackupManagerContext.class);
  }
}
