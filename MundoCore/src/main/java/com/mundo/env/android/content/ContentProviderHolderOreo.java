package com.mundo.env.android.content;

import android.content.pm.ProviderInfo;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.ContentProviderHolder")
public interface ContentProviderHolderOreo {
    @BField
    ProviderInfo info();

    @BField
    boolean noReleaseNeeded();

    @BField
    IInterface provider();
}
