package com.mundo.env.android.net.wifi;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.net.wifi.WifiManager")
public interface WifiManager {
    @BStaticField
    IInterface sService();

    @BField
    IInterface mService();
}
