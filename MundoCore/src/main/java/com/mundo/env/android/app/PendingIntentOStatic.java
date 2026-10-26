package com.mundo.env.android.app;

import android.os.IBinder;
import java.lang.Object;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.app.PendingIntent")
public interface PendingIntentOStatic {
  @BConstructorNotProcess
  PendingIntentO _new(IBinder IBinder0, Object Object1);
}
