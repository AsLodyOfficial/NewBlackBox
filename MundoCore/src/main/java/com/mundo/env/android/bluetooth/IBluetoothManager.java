package com.mundo.env.android.bluetooth;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.bluetooth.IBluetoothManager")
public interface IBluetoothManager {
    @BClassName("android.bluetooth.IBluetoothManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
