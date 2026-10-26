package com.mundo.env.android.app;

import android.app.Application;
import android.os.IInterface;
import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.app.ActivityThread")
public interface ActivityThreadStatic {
  @BMethodCheckNotProcess
  Method _check_currentActivityThread();

  Object currentActivityThread();

  @BMethodCheckNotProcess
  Method _check_currentApplication();

  Application currentApplication();

  @BMethodCheckNotProcess
  Method _check_currentPackageName();

  String currentPackageName();

  @BFieldSetNotProcess
  void _set_sPackageManager(final Object value);

  @BFieldCheckNotProcess
  Field _check_sPackageManager();

  @BFieldNotProcess
  IInterface sPackageManager();

  @BFieldSetNotProcess
  void _set_sPermissionManager(final Object value);

  @BFieldCheckNotProcess
  Field _check_sPermissionManager();

  @BFieldNotProcess
  IInterface sPermissionManager();
}
