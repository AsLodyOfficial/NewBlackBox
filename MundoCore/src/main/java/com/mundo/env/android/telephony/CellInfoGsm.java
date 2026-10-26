package com.mundo.env.android.telephony;

import android.telephony.CellIdentityGsm;
import android.telephony.CellSignalStrengthGsm;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.telephony.CellInfoGsm")
public interface CellInfoGsm {
    @BConstructor
    CellInfoGsm _new();

    @BField
    CellIdentityGsm mCellIdentityGsm();

    @BField
    CellSignalStrengthGsm mCellSignalStrengthGsm();
}
