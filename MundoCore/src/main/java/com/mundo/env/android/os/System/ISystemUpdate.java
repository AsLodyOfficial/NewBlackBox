package com.mundo.env.android.os.System;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.os.ISystemUpdate")
public interface ISystemUpdate {

    @BClassName("android.os.ISystemUpdate$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder binder);
    }
}

