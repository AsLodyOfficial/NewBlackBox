package com.mundo.env.android.hardware.location;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.hardware.location.IContextHubService$Stub")
public interface IContextHubServiceStubStatic {
  @BMethodCheckNotProcess
  Method _check_asInterface(IBinder iBinder);

  IInterface asInterface(IBinder iBinder);
}
