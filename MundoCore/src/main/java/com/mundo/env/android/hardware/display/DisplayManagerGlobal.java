package com.mundo.env.android.hardware.display;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.hardware.display.DisplayManagerGlobal")
public interface DisplayManagerGlobal {
    @BField
    IInterface mDm();

    @BStaticMethod
    Object getInstance();
}
