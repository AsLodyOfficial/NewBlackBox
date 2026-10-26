package com.mundo.env.android.net;

import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.net.NetworkInfo")
public interface NetworkInfo {
    @BConstructor
    NetworkInfo _new(int type);

    @BConstructor
    NetworkInfo _new(int type, int subType, String typeName, String subTypeName);

    @BField
    DetailedState mDetailedState();

    @BField
    State mState();

    @BField
    boolean mIsAvailable();

    @BField
    String mTypeName();

    @BField
    int mNetworkType();
}
