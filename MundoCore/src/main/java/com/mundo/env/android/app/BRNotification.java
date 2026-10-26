package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRNotification {
  public static NotificationStatic getWithException() {
    return BlackReflection.create(NotificationStatic.class, null, true);
  }

  public static NotificationStatic get() {
    return BlackReflection.create(NotificationStatic.class, null, false);
  }

  public static NotificationContext getWithException(final Object caller) {
    return BlackReflection.create(NotificationContext.class, caller, true);
  }

  public static NotificationContext get(final Object caller) {
    return BlackReflection.create(NotificationContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(NotificationContext.class);
  }
}
