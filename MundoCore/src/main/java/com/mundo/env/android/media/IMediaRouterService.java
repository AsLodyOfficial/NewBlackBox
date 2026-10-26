package com.mundo.env.android.media;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.media.IMediaRouterService")
public interface IMediaRouterService {
    @BClassName("android.media.IMediaRouterService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
