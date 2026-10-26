package com.mundo.env.android.app;

import java.lang.Object;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.app.ActivityTaskManager")
public interface ActivityTaskManagerStatic {
  @BMethodCheckNotProcess
  Method _check_getService();

  Object getService();

  @BFieldSetNotProcess
  void _set_IActivityTaskManagerSingleton(final Object value);

  @BFieldCheckNotProcess
  Field _check_IActivityTaskManagerSingleton();

  @BFieldNotProcess
  Object IActivityTaskManagerSingleton();
}
