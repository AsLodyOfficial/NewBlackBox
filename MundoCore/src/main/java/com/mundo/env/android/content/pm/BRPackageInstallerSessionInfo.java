package com.mundo.env.android.content.pm;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRPackageInstallerSessionInfo {
  public static PackageInstallerSessionInfoStatic getWithException() {
    return BlackReflection.create(PackageInstallerSessionInfoStatic.class, null, true);
  }

  public static PackageInstallerSessionInfoStatic get() {
    return BlackReflection.create(PackageInstallerSessionInfoStatic.class, null, false);
  }

  public static PackageInstallerSessionInfoContext getWithException(final Object caller) {
    return BlackReflection.create(PackageInstallerSessionInfoContext.class, caller, true);
  }

  public static PackageInstallerSessionInfoContext get(final Object caller) {
    return BlackReflection.create(PackageInstallerSessionInfoContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(PackageInstallerSessionInfoContext.class);
  }
}
