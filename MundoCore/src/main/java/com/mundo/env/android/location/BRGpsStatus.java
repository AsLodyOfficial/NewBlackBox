package com.mundo.env.android.location;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRGpsStatus {
  public static GpsStatusStatic getWithException() {
    return BlackReflection.create(GpsStatusStatic.class, null, true);
  }

  public static GpsStatusStatic get() {
    return BlackReflection.create(GpsStatusStatic.class, null, false);
  }

  public static GpsStatusContext getWithException(final Object caller) {
    return BlackReflection.create(GpsStatusContext.class, caller, true);
  }

  public static GpsStatusContext get(final Object caller) {
    return BlackReflection.create(GpsStatusContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(GpsStatusContext.class);
  }
}
