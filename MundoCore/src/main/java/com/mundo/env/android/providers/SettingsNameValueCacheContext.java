package com.mundo.env.android.providers;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.provider.Settings$NameValueCache")
public interface SettingsNameValueCacheContext {
  @BFieldSetNotProcess
  void _set_mContentProvider(final Object value);

  @BFieldCheckNotProcess
  Field _check_mContentProvider();

  @BFieldNotProcess
  Object mContentProvider();
}
