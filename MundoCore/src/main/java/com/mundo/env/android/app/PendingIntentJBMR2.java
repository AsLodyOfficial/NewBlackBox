package com.mundo.env.android.app;

import android.content.Intent;
import android.os.IBinder;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.app.PendingIntent")
public interface PendingIntentJBMR2 {
    @BConstructor
    PendingIntentJBMR2 _new(IBinder IBinder0);

    @BMethod
    Intent getIntent();
}
