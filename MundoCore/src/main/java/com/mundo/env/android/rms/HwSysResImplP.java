package com.mundo.env.android.rms;

import java.util.Map;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.rms.HwSysResImpl")
public interface HwSysResImplP {
    @BField
    Map<Integer, java.util.ArrayList<String>> mWhiteListMap();
}
