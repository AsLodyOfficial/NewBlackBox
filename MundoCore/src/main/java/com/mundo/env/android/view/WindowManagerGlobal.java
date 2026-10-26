package com.mundo.env.android.view;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.view.WindowManagerGlobal")
public interface WindowManagerGlobal {
    @BStaticField
    int ADD_PERMISSION_DENIED();

    @BStaticField
    IInterface sWindowManagerService();
}
