package com.mundo.env.android.rms.resource;

import java.util.Map;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceO {
    @BField
    Map<Integer, java.util.List<String>> mWhiteListMap();
}
