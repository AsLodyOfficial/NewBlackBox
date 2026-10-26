package com.mundo.env.android.net.wifi;

import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.net.wifi.WifiInfo")
public interface WifiInfoStatic {
  @BConstructorNotProcess
  WifiInfo _new();
}
