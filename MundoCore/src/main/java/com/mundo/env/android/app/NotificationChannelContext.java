package com.mundo.env.android.app;

import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.NotificationChannel")
public interface NotificationChannelContext {
  @BFieldSetNotProcess
  void _set_mId(final Object value);

  @BFieldCheckNotProcess
  Field _check_mId();

  @BFieldNotProcess
  String mId();
}
