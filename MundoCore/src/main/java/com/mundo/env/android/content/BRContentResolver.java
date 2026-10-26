package com.mundo.env.android.content;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRContentResolver {
  public static ContentResolverStatic getWithException() {
    return BlackReflection.create(ContentResolverStatic.class, null, true);
  }

  public static ContentResolverStatic get() {
    return BlackReflection.create(ContentResolverStatic.class, null, false);
  }

  public static ContentResolverContext getWithException(final Object caller) {
    return BlackReflection.create(ContentResolverContext.class, caller, true);
  }

  public static ContentResolverContext get(final Object caller) {
    return BlackReflection.create(ContentResolverContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(ContentResolverContext.class);
  }
}
