package com.mundo.env.android.location;

import android.util.ArrayMap;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.location.LocationManager")
public interface LocationManagerQ {
    @BField
    ArrayMap mGnssNmeaListeners();

    @BField
    ArrayMap mGnssStatusListeners();

    @BField
    ArrayMap mGpsNmeaListeners();

    @BField
    ArrayMap mGpsStatusListeners();

    @BField
    ArrayMap mListeners();
}
