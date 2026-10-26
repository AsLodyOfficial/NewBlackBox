package com.mundo.env.android.os;

import java.lang.Class;
import java.lang.Object;
import com.mundo.reflection.BlackReflection;

public class BRMessage {
  public static MessageStatic getWithException() {
    return BlackReflection.create(MessageStatic.class, null, true);
  }

  public static MessageStatic get() {
    return BlackReflection.create(MessageStatic.class, null, false);
  }

  public static MessageContext getWithException(final Object caller) {
    return BlackReflection.create(MessageContext.class, caller, true);
  }

  public static MessageContext get(final Object caller) {
    return BlackReflection.create(MessageContext.class, caller, false);
  }

  public static Class getRealClass() {
    return com.mundo.reflection.utils.ClassUtil.classReady(MessageContext.class);
  }
}
