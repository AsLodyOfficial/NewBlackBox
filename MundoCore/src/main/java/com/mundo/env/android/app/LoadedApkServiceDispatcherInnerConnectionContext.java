package com.mundo.env.android.app;

import java.lang.Object;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.LoadedApk$ServiceDispatcher$InnerConnection")
public interface LoadedApkServiceDispatcherInnerConnectionContext {
  @BFieldSetNotProcess
  void _set_mDispatcher(final Object value);

  @BFieldCheckNotProcess
  Field _check_mDispatcher();

  @BFieldNotProcess
  WeakReference<?> mDispatcher();
}
