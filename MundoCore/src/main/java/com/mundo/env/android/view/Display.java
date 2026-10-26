package com.mundo.env.android.view;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.view.Display")
public interface Display {
    @BStaticField
    IInterface sWindowManager();
}
