package com.mundo.env.android.net;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.net.IConnectivityManager$Stub")
public interface IConnectivityManagerStubStatic {
  @BMethodCheckNotProcess
  Method _check_asInterface(IBinder binder);

  IInterface asInterface(IBinder binder);
}
