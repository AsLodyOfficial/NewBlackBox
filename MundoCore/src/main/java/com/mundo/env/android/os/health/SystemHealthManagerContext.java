package com.mundo.env.android.os.health;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.os.health.SystemHealthManager")
public interface SystemHealthManagerContext {
  @BFieldSetNotProcess
  void _set_mBatteryStats(final Object value);

  @BFieldCheckNotProcess
  Field _check_mBatteryStats();

  @BFieldNotProcess
  IInterface mBatteryStats();
}
