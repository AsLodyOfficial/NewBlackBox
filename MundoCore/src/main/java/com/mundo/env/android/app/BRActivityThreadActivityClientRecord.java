package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRActivityThreadActivityClientRecord {
  public static ActivityThreadActivityClientRecordStatic getWithException() {
    return BlackReflection.create(ActivityThreadActivityClientRecordStatic.class, null, true);
  }

  public static ActivityThreadActivityClientRecordStatic get() {
    return BlackReflection.create(ActivityThreadActivityClientRecordStatic.class, null, false);
  }

  public static ActivityThreadActivityClientRecordContext getWithException(final Object caller) {
    return BlackReflection.create(ActivityThreadActivityClientRecordContext.class, caller, true);
  }

  public static ActivityThreadActivityClientRecordContext get(final Object caller) {
    return BlackReflection.create(ActivityThreadActivityClientRecordContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ActivityThreadActivityClientRecordContext.class);
  }
}
