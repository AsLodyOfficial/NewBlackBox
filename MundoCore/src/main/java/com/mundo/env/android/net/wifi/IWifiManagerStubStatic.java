package com.mundo.env.android.net.wifi;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.net.wifi.IWifiManager$Stub")
public interface IWifiManagerStubStatic {
  @BMethodCheckNotProcess
  Method _check_asInterface(IBinder IBinder0);

  IInterface asInterface(IBinder IBinder0);
}
