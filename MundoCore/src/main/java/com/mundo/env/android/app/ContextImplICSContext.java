package com.mundo.env.android.app;

import java.io.File;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.ContextImpl")
public interface ContextImplICSContext {
  @BFieldSetNotProcess
  void _set_mExternalCacheDir(final Object value);

  @BFieldCheckNotProcess
  Field _check_mExternalCacheDir();

  @BFieldNotProcess
  File mExternalCacheDir();

  @BFieldSetNotProcess
  void _set_mExternalFilesDir(final Object value);

  @BFieldCheckNotProcess
  Field _check_mExternalFilesDir();

  @BFieldNotProcess
  File mExternalFilesDir();
}
