package com.mundo.env.android.widget;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.widget.Toast")
public interface Toast {
    @BStaticField
    IInterface sService();
}
