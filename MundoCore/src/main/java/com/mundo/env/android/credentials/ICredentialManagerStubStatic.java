package com.mundo.env.android.credentials;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.credentials.ICredentialManager$Stub")
public interface ICredentialManagerStubStatic {
  @BMethodCheckNotProcess
  Method _check_asInterface(IBinder binder);

  IInterface asInterface(IBinder binder);
}
