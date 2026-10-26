package com.mundo.env.android.content.pm;

import android.content.pm.PackageParser.SigningDetails;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.content.pm.SigningInfo")
public interface SigningInfo {
    @BConstructor
    android.content.pm.SigningInfo _new(SigningDetails SigningDetails0);

    @BField
    SigningDetails mSigningDetails();
}
