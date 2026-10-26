package com.mundo.env.android.app;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRIUsageStatsManager {
  public static IUsageStatsManagerStatic getWithException() {
    return BlackReflection.create(IUsageStatsManagerStatic.class, null, true);
  }

  public static IUsageStatsManagerStatic get() {
    return BlackReflection.create(IUsageStatsManagerStatic.class, null, false);
  }

  public static IUsageStatsManagerContext getWithException(final Object caller) {
    return BlackReflection.create(IUsageStatsManagerContext.class, caller, true);
  }

  public static IUsageStatsManagerContext get(final Object caller) {
    return BlackReflection.create(IUsageStatsManagerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(IUsageStatsManagerContext.class);
  }
}
