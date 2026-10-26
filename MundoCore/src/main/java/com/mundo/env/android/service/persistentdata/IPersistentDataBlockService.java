package com.mundo.env.android.service.persistentdata;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.service.persistentdata.IPersistentDataBlockService")
public interface IPersistentDataBlockService {
    @BClassName("android.service.persistentdata.IPersistentDataBlockService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
