package com.mundo.env.android.content;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.content.IntentSender")
public interface IntentSender {
    @BField
    IInterface mTarget();
}
