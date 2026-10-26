package com.mundo.env.android.telephony;

import android.telephony.CellIdentityCdma;
import android.telephony.CellSignalStrengthCdma;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.telephony.CellInfoCdma")
public interface CellInfoCdma {
    @BConstructor
    CellInfoCdma _new();

    @BField
    CellIdentityCdma mCellIdentityCdma();

    @BField
    CellSignalStrengthCdma mCellSignalStrengthCdma();
}
