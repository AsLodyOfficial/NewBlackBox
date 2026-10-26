package com.mundo.env.android.os;

import android.os.Parcel;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.os.BaseBundle")
public interface BaseBundle {
    @BField
    Parcel mParcelledData();
}
