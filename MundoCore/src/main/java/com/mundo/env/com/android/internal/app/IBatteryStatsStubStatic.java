package com.mundo.env.com.android.internal.app;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("com.android.internal.app.IBatteryStats$Stub")
public interface IBatteryStatsStubStatic {
  @BMethodCheckNotProcess
  Method _check_asInterface(IBinder IBinder0);

  IInterface asInterface(IBinder IBinder0);
}
