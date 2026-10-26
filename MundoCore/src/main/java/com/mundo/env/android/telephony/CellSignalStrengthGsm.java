package com.mundo.env.android.telephony;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.telephony.CellSignalStrengthGsm")
public interface CellSignalStrengthGsm {
    @BConstructor
    CellSignalStrengthGsm _new();

    @BField
    int mBitErrorRate();

    @BField
    int mSignalStrength();
}
