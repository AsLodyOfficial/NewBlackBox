package com.mundo.env.android.app;

import android.os.IBinder;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;

@BClassName("android.app.PendingIntent")
public interface PendingIntentO {
    @BConstructor
    PendingIntentO _new(IBinder IBinder0, Object Object1);
}
