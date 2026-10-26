package com.mundo.env.android.app;

import android.content.Intent;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.ServiceStartArgs")
public interface ServiceStartArgsContext {
  @BFieldSetNotProcess
  void _set_args(final Object value);

  @BFieldCheckNotProcess
  Field _check_args();

  @BFieldNotProcess
  Intent args();

  @BFieldSetNotProcess
  void _set_flags(final Object value);

  @BFieldCheckNotProcess
  Field _check_flags();

  @BFieldNotProcess
  Integer flags();

  @BFieldSetNotProcess
  void _set_startId(final Object value);

  @BFieldCheckNotProcess
  Field _check_startId();

  @BFieldNotProcess
  Integer startId();

  @BFieldSetNotProcess
  void _set_taskRemoved(final Object value);

  @BFieldCheckNotProcess
  Field _check_taskRemoved();

  @BFieldNotProcess
  Boolean taskRemoved();
}
