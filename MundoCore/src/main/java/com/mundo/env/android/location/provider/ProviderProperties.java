package com.mundo.env.android.location.provider;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.location.provider.ProviderProperties")
public interface ProviderProperties {
    @BField
    boolean mHasNetworkRequirement();

    @BField
    boolean mHasSatelliteRequirement();

    @BField
    boolean mHasCellRequirement();

    @BField
    boolean mHasMonetaryCost();

    @BField
    boolean mHasAltitudeSupport();

    @BField
    boolean mHasSpeedSupport();

    @BField
    boolean mHasBearingSupport();
}
