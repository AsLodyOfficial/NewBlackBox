package com.mundo.env.android.app.job;

import android.content.ComponentName;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.job.JobInfo")
public interface JobInfoContext {
  @BFieldSetNotProcess
  void _set_flexMillis(final Object value);

  @BFieldCheckNotProcess
  Field _check_flexMillis();

  @BFieldNotProcess
  Long flexMillis();

  @BFieldSetNotProcess
  void _set_intervalMillis(final Object value);

  @BFieldCheckNotProcess
  Field _check_intervalMillis();

  @BFieldNotProcess
  Long intervalMillis();

  @BFieldSetNotProcess
  void _set_jobId(final Object value);

  @BFieldCheckNotProcess
  Field _check_jobId();

  @BFieldNotProcess
  Integer jobId();

  @BFieldSetNotProcess
  void _set_service(final Object value);

  @BFieldCheckNotProcess
  Field _check_service();

  @BFieldNotProcess
  ComponentName service();
}
