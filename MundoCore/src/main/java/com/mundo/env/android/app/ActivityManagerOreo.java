package com.mundo.env.android.app;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.app.ActivityManager")
public interface ActivityManagerOreo {
    @BStaticField
    Object IActivityManagerSingleton();

    @BStaticMethod
    IInterface getService();
}
