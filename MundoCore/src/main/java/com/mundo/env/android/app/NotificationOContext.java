package com.mundo.env.android.app;

import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.Notification")
public interface NotificationOContext {
  @BFieldSetNotProcess
  void _set_mChannelId(final Object value);

  @BFieldCheckNotProcess
  Field _check_mChannelId();

  @BFieldNotProcess
  String mChannelId();

  @BFieldSetNotProcess
  void _set_mGroupKey(final Object value);

  @BFieldCheckNotProcess
  Field _check_mGroupKey();

  @BFieldNotProcess
  String mGroupKey();
}
