package com.mundo.env.android.app;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.LoadedApk")
public interface LoadedApkKitkatContext {
  @BFieldSetNotProcess
  void _set_mDisplayAdjustments(final Object value);

  @BFieldCheckNotProcess
  Field _check_mDisplayAdjustments();

  @BFieldNotProcess
  Object mDisplayAdjustments();
}
