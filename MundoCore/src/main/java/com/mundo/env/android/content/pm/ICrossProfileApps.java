package com.mundo.env.android.content.pm;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

/**
 * @author gm
 * @function
 * @date :2024/4/20 22:14
 **/
@BClassName("android.content.pm.ICrossProfileApps")
public interface ICrossProfileApps {
    @BClassName("android.content.pm.ICrossProfileApps$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
