package com.mundo.env.android.os;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.os.Parcel")
public interface Parcel {
    @BStaticField
    int VAL_PARCELABLE();

    @BStaticField
    int VAL_PARCELABLEARRAY();
}
