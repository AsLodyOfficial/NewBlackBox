package com.mundo.env.android.media.session;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.media.session.ISessionManager")
public interface ISessionManager {
    @BClassName("android.media.session.ISessionManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }

    // Add a method to get the ISessionManager instance dynamically for Android 10+
    @BStaticMethod
    IInterface getService(Context context);
}
