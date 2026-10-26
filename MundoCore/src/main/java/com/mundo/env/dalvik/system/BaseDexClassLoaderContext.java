package com.mundo.env.dalvik.system;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("dalvik.system.BaseDexClassLoader")
public interface BaseDexClassLoaderContext {
  @BFieldSetNotProcess
  void _set_sharedLibraryLoaders(final Object value);

  @BFieldCheckNotProcess
  Field _check_sharedLibraryLoaders();

  @BFieldNotProcess
  Object sharedLibraryLoaders();
}
