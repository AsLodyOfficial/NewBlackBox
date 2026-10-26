package com.mundo.env.android.hardware.display;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.hardware.display.DisplayManagerGlobal")
public interface DisplayManagerGlobalContext {
  @BFieldSetNotProcess
  void _set_mDm(final Object value);

  @BFieldCheckNotProcess
  Field _check_mDm();

  @BFieldNotProcess
  IInterface mDm();
}
