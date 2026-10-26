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

@BClassNameNotProcess("android.app.ActivityManagerNative")
public interface ActivityManagerNativeStatic {
  @BMethodCheckNotProcess
  Method _check_getDefault();

  IInterface getDefault();

  @BFieldSetNotProcess
  void _set_gDefault(final Object value);

  @BFieldCheckNotProcess
  Field _check_gDefault();

  @BFieldNotProcess
  Object gDefault();
}
