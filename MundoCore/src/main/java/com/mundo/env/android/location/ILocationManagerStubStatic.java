package com.mundo.env.android.location;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.location.ILocationManager$Stub")
public interface ILocationManagerStubStatic {
  @BMethodCheckNotProcess
  Method _check_asInterface(IBinder IBinder0);

  IInterface asInterface(IBinder IBinder0);
}
