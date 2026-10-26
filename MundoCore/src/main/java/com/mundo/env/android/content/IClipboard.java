package com.mundo.env.android.content;

import android.os.IBinder;
import android.os.IInterface;
import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.content.IClipboard")
public interface IClipboard {

    @BClassName("android.content.IClipboard$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder iBinder);
    }
}
