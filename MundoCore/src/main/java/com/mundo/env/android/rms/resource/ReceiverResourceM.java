package com.mundo.env.android.rms.resource;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceM {
    @BField
    String[] mWhiteList();
}
