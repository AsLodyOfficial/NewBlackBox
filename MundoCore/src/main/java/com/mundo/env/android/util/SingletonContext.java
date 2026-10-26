package com.mundo.env.android.util;

import java.lang.Object;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.util.Singleton")
public interface SingletonContext {
  @BMethodCheckNotProcess
  Method _check_get();

  Object get();

  @BFieldSetNotProcess
  void _set_mInstance(final Object value);

  @BFieldCheckNotProcess
  Field _check_mInstance();

  @BFieldNotProcess
  Object mInstance();
}
