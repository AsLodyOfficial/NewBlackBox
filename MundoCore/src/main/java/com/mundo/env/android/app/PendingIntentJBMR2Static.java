package com.mundo.env.android.app;

import android.os.IBinder;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.app.PendingIntent")
public interface PendingIntentJBMR2Static {
  @BConstructorNotProcess
  PendingIntentJBMR2 _new(IBinder IBinder0);
}
