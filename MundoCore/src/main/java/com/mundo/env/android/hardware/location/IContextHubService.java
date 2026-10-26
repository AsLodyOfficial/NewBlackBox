package com.mundo.env.android.hardware.location;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

/**
 * Created by BlackBox on 2022/3/2.
 */
@BClassName("android.hardware.location.IContextHubService")
public interface IContextHubService {

    @BClassName("android.hardware.location.IContextHubService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder iBinder);
    }
}
