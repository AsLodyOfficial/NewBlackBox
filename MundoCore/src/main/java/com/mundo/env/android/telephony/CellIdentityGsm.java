package com.mundo.env.android.telephony;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.telephony.CellIdentityGsm")
public interface CellIdentityGsm {
    @BConstructor
    CellIdentityGsm _new();

    @BField
    int mCid();

    @BField
    int mLac();

    @BField
    int mMcc();

    @BField
    int mMnc();
}
