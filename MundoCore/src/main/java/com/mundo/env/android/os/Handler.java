package com.mundo.env.android.os;

import android.os.Handler.Callback;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.os.Handler")
public interface Handler {
    @BField
    Callback mCallback();
}
