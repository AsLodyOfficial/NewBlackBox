package com.mundo.env.android.telephony;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.telephony.CellIdentityCdma")
public interface CellIdentityCdma {
    @BConstructor
    CellIdentityCdma _new();

    @BField
    int mBasestationId();

    @BField
    int mNetworkId();

    @BField
    int mSystemId();
}
