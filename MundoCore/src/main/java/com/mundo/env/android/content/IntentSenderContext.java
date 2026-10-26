package com.mundo.env.android.content;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.IntentSender")
public interface IntentSenderContext {
  @BFieldSetNotProcess
  void _set_mTarget(final Object value);

  @BFieldCheckNotProcess
  Field _check_mTarget();

  @BFieldNotProcess
  IInterface mTarget();
}
