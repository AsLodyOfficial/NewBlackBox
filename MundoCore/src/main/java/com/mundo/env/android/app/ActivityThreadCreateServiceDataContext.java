package com.mundo.env.android.app;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.ActivityThread$CreateServiceData")
public interface ActivityThreadCreateServiceDataContext {
  @BFieldSetNotProcess
  void _set_compatInfo(final Object value);

  @BFieldCheckNotProcess
  Field _check_compatInfo();

  @BFieldNotProcess
  Object compatInfo();

  @BFieldSetNotProcess
  void _set_info(final Object value);

  @BFieldCheckNotProcess
  Field _check_info();

  @BFieldNotProcess
  ServiceInfo info();

  @BFieldSetNotProcess
  void _set_intent(final Object value);

  @BFieldCheckNotProcess
  Field _check_intent();

  @BFieldNotProcess
  Intent intent();

  @BFieldSetNotProcess
  void _set_token(final Object value);

  @BFieldCheckNotProcess
  Field _check_token();

  @BFieldNotProcess
  IBinder token();
}
