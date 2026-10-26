package com.mundo.env.com.android.internal.policy;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("com.android.internal.policy.PhoneWindow$WindowManagerHolder")
public interface PhoneWindowStatic {
  @BFieldSetNotProcess
  void _set_sWindowManager(final Object value);

  @BFieldCheckNotProcess
  Field _check_sWindowManager();

  @BFieldNotProcess
  IInterface sWindowManager();
}
