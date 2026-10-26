package com.mundo.env.android.app;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.AlarmManager")
public interface AlarmManager {
    @BField
    IInterface mService();

    @BField
    int mTargetSdkVersion();
}
