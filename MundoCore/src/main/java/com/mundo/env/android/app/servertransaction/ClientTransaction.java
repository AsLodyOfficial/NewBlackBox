package com.mundo.env.android.app.servertransaction;

import android.os.IBinder;

import java.util.List;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.servertransaction.ClientTransaction")
public interface ClientTransaction {
    @BField
    List<Object> mActivityCallbacks();

    @BField
    IBinder mActivityToken();

    @BField
    Object mLifecycleStateRequest();
}
