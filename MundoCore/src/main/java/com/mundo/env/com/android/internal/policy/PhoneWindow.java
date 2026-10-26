package com.mundo.env.com.android.internal.policy;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("com.android.internal.policy.PhoneWindow$WindowManagerHolder")
public interface PhoneWindow {
    @BStaticField
    IInterface sWindowManager();
}
