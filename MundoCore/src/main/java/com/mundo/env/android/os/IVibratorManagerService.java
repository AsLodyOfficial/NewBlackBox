package com.mundo.env.android.os;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

/**
 * Created by BlackBox on 2022/3/19.
 */
@BClassName("android.os.IVibratorManagerService")
public interface IVibratorManagerService {

    @BClassName("android.os.IVibratorManagerService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
