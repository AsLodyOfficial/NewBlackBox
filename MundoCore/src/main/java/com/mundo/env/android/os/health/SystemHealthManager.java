package com.mundo.env.android.os.health;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.os.health.SystemHealthManager")
public interface SystemHealthManager {
    @BField
    IInterface mBatteryStats();
}
