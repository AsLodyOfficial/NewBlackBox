package com.mundo.env.android.app;

import android.os.IBinder;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.app.ClientTransactionHandler")
public interface ClientTransactionHandler {
    @BMethod
    Object getActivityClient(IBinder IBinder0);
}
