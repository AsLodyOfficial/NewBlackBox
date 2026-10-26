package com.mundo.env.android.location;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRLocationManagerGpsStatusListenerTransportSumsungS5 {
  public static LocationManagerGpsStatusListenerTransportSumsungS5Static getWithException() {
    return BlackReflection.create(LocationManagerGpsStatusListenerTransportSumsungS5Static.class, null, true);
  }

  public static LocationManagerGpsStatusListenerTransportSumsungS5Static get() {
    return BlackReflection.create(LocationManagerGpsStatusListenerTransportSumsungS5Static.class, null, false);
  }

  public static LocationManagerGpsStatusListenerTransportSumsungS5Context getWithException(final Object caller) {
    return BlackReflection.create(LocationManagerGpsStatusListenerTransportSumsungS5Context.class, caller, true);
  }

  public static LocationManagerGpsStatusListenerTransportSumsungS5Context get(final Object caller) {
    return BlackReflection.create(LocationManagerGpsStatusListenerTransportSumsungS5Context.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(LocationManagerGpsStatusListenerTransportSumsungS5Context.class);
  }
}
