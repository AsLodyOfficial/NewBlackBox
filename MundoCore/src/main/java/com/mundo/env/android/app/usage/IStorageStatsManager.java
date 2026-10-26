package com.mundo.env.android.app.usage;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.app.usage.IStorageStatsManager")
public interface IStorageStatsManager {
    @BClassName("android.app.usage.IStorageStatsManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
