package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRNotificationL {
  public static NotificationLStatic getWithException() {
    return BlackReflection.create(NotificationLStatic.class, null, true);
  }

  public static NotificationLStatic get() {
    return BlackReflection.create(NotificationLStatic.class, null, false);
  }

  public static NotificationLContext getWithException(final Object caller) {
    return BlackReflection.create(NotificationLContext.class, caller, true);
  }

  public static NotificationLContext get(final Object caller) {
    return BlackReflection.create(NotificationLContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(NotificationLContext.class);
  }
}
