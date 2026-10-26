package com.mundo.env.android.content;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.content.ClipboardManager")
public interface ClipboardManagerOreo {
    @BStaticField
    IInterface sService();

    @BField
    IInterface mService();
}
