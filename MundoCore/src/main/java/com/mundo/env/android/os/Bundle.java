package com.mundo.env.android.os;

import android.os.IBinder;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.os.Bundle")
public interface Bundle {
    @BMethod
    IBinder getIBinder(String String0);

    @BMethod
    void putIBinder(String String0, IBinder IBinder1);
}
