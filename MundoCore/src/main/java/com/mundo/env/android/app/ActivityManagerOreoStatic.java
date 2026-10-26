package com.mundo.env.android.app;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.app.ActivityManager")
public interface ActivityManagerOreoStatic {
  @BMethodCheckNotProcess
  Method _check_getService();

  IInterface getService();

  @BFieldSetNotProcess
  void _set_IActivityManagerSingleton(final Object value);

  @BFieldCheckNotProcess
  Field _check_IActivityManagerSingleton();

  @BFieldNotProcess
  Object IActivityManagerSingleton();
}
