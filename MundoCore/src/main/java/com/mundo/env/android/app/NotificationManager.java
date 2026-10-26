package com.mundo.env.android.app;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.app.NotificationManager")
public interface NotificationManager {
    @BStaticField
    IInterface sService();

    @BStaticMethod
    IInterface getService();
}
