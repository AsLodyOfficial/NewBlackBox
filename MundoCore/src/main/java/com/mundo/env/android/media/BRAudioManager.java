package com.mundo.env.android.media;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRAudioManager {
  public static AudioManagerStatic getWithException() {
    return BlackReflection.create(AudioManagerStatic.class, null, true);
  }

  public static AudioManagerStatic get() {
    return BlackReflection.create(AudioManagerStatic.class, null, false);
  }

  public static AudioManagerContext getWithException(final Object caller) {
    return BlackReflection.create(AudioManagerContext.class, caller, true);
  }

  public static AudioManagerContext get(final Object caller) {
    return BlackReflection.create(AudioManagerContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(AudioManagerContext.class);
  }
}
