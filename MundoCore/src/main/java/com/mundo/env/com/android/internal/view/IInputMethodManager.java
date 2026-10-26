package com.mundo.env.com.android.internal.view;


import android.os.IBinder;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

//这里可能有点问题
@BClassName("com.android.internal.view.IInputMethodManager")
public interface IInputMethodManager {
    @BClassName("com.android.internal.view.IInputMethodManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
