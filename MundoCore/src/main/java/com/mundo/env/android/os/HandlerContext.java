package com.mundo.env.android.os;

import android.os.Handler;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.os.Handler")
public interface HandlerContext {
  @BFieldSetNotProcess
  void _set_mCallback(final Object value);

  @BFieldCheckNotProcess
  Field _check_mCallback();

  @BFieldNotProcess
  Handler.Callback mCallback();
}
