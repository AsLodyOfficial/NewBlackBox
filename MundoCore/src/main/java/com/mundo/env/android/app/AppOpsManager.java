package com.mundo.env.android.app;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.AppOpsManager")
public interface AppOpsManager {
    @BField
    IInterface mService();
}
