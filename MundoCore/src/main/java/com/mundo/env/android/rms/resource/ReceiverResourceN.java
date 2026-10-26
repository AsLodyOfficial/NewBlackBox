package com.mundo.env.android.rms.resource;

import java.util.List;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceN {
    @BField
    List<String> mWhiteList();
}
