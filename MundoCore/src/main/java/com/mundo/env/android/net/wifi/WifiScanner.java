package com.mundo.env.android.net.wifi;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.net.wifi.WifiScanner")
public interface WifiScanner {
    @BStaticField
    String GET_AVAILABLE_CHANNELS_EXTRA();
}
