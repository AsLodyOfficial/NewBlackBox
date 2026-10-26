package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRActivityThreadProviderClientRecordJB {
  public static ActivityThreadProviderClientRecordJBStatic getWithException() {
    return BlackReflection.create(ActivityThreadProviderClientRecordJBStatic.class, null, true);
  }

  public static ActivityThreadProviderClientRecordJBStatic get() {
    return BlackReflection.create(ActivityThreadProviderClientRecordJBStatic.class, null, false);
  }

  public static ActivityThreadProviderClientRecordJBContext getWithException(final Object caller) {
    return BlackReflection.create(ActivityThreadProviderClientRecordJBContext.class, caller, true);
  }

  public static ActivityThreadProviderClientRecordJBContext get(final Object caller) {
    return BlackReflection.create(ActivityThreadProviderClientRecordJBContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ActivityThreadProviderClientRecordJBContext.class);
  }
}
