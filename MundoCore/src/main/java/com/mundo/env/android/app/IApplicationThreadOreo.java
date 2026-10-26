package com.mundo.env.android.app;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.app.IApplicationThread")
public interface IApplicationThreadOreo {
    @BMethod
    void scheduleServiceArgs();

    @BClassName("android.app.IApplicationThread$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
