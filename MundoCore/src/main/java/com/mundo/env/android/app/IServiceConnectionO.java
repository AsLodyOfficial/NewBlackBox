package com.mundo.env.android.app;

import android.content.ComponentName;
import android.os.IBinder;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.app.IServiceConnection")
public interface IServiceConnectionO {
    @BMethod
    void connected(ComponentName ComponentName0, IBinder IBinder1, boolean boolean2);
}
