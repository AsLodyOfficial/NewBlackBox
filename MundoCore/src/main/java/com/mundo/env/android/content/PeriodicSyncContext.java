package com.mundo.env.android.content;

import java.lang.Long;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.PeriodicSync")
public interface PeriodicSyncContext {
  @BFieldSetNotProcess
  void _set_flexTime(final Object value);

  @BFieldCheckNotProcess
  Field _check_flexTime();

  @BFieldNotProcess
  Long flexTime();
}
