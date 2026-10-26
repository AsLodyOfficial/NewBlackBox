package com.mundo.env.android.app;

import android.os.IBinder;
import java.lang.Object;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.app.ClientTransactionHandler")
public interface ClientTransactionHandlerContext {
  @BMethodCheckNotProcess
  Method _check_getActivityClient(IBinder IBinder0);

  Object getActivityClient(IBinder IBinder0);
}
