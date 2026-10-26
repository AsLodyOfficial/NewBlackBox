package com.mundo.env.android.app;

import java.lang.Object;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.app.ContextImpl")
public interface ContextImplStatic {
  @BMethodCheckNotProcess
  Method _check_createAppContext();

  Object createAppContext();
}
