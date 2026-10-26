package com.mundo.env.android.app;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

/**
 * @author gm
 * @function
 * @date :2024/4/20 20:04
 **/
@BClassName("android.app.ILocaleManager")
public interface ILocaleManager {
    @BClassName("android.app.ILocaleManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
