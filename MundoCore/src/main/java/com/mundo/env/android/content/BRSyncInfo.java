package com.mundo.env.android.content;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRSyncInfo {
  public static SyncInfoStatic getWithException() {
    return BlackReflection.create(SyncInfoStatic.class, null, true);
  }

  public static SyncInfoStatic get() {
    return BlackReflection.create(SyncInfoStatic.class, null, false);
  }

  public static SyncInfoContext getWithException(final Object caller) {
    return BlackReflection.create(SyncInfoContext.class, caller, true);
  }

  public static SyncInfoContext get(final Object caller) {
    return BlackReflection.create(SyncInfoContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(SyncInfoContext.class);
  }
}
