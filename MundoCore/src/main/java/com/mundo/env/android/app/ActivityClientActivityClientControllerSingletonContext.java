package com.mundo.env.android.app;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.ActivityClient$ActivityClientControllerSingleton")
public interface ActivityClientActivityClientControllerSingletonContext {
  @BFieldSetNotProcess
  void _set_mKnownInstance(final Object value);

  @BFieldCheckNotProcess
  Field _check_mKnownInstance();

  @BFieldNotProcess
  IInterface mKnownInstance();
}
