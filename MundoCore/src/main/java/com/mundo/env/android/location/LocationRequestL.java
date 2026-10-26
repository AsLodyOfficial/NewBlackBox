package com.mundo.env.android.location;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.location.LocationRequest")
public interface LocationRequestL {
    @BField
    boolean mHideFromAppOps();

    @BField
    String mProvider();

    @BField
    Object mWorkSource();

    @BMethod
    String getProvider();
}
