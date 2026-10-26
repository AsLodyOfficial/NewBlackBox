package com.mundo.env.android.content.pm;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRILauncherAppsStub {
  public static ILauncherAppsStubStatic getWithException() {
    return BlackReflection.create(ILauncherAppsStubStatic.class, null, true);
  }

  public static ILauncherAppsStubStatic get() {
    return BlackReflection.create(ILauncherAppsStubStatic.class, null, false);
  }

  public static ILauncherAppsStubContext getWithException(final Object caller) {
    return BlackReflection.create(ILauncherAppsStubContext.class, caller, true);
  }

  public static ILauncherAppsStubContext get(final Object caller) {
    return BlackReflection.create(ILauncherAppsStubContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ILauncherAppsStubContext.class);
  }
}
