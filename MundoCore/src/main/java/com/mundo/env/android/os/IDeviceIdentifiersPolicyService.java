package com.mundo.env.android.os;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.os.IDeviceIdentifiersPolicyService")
public interface IDeviceIdentifiersPolicyService {
    @BClassName("android.os.IDeviceIdentifiersPolicyService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
