package com.mundo.env.dalvik.system;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("dalvik.system.BaseDexClassLoader")
public interface BaseDexClassLoader {

  @BField
  Object sharedLibraryLoaders();
}
