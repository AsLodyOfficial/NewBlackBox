package com.mundo.env.android.os;

import android.os.Parcel;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.os.Bundle")
public interface BundleICS {
    @BField
    Parcel mParcelledData();
}
