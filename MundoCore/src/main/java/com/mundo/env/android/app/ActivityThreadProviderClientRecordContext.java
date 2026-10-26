package com.mundo.env.android.app;

import android.os.IInterface;
import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.ActivityThread$ProviderClientRecord")
public interface ActivityThreadProviderClientRecordContext {
  @BFieldSetNotProcess
  void _set_mName(final Object value);

  @BFieldCheckNotProcess
  Field _check_mName();

  @BFieldNotProcess
  String mName();

  @BFieldSetNotProcess
  void _set_mProvider(final Object value);

  @BFieldCheckNotProcess
  Field _check_mProvider();

  @BFieldNotProcess
  IInterface mProvider();
}
