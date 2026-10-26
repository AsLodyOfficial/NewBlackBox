package com.mundo.env.android.content.pm;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.content.pm.IShortcutService")
public interface IShortcutService {
    @BClassName("android.content.pm.IShortcutService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
