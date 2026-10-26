package com.mundo.env.android.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BMethod;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.app.ContextImpl")
public interface ContextImpl {
    @BField
    String mBasePackageName();

    @BField
    ContentResolver mContentResolver();

    @BField
    Object mPackageInfo();

    @BField
    PackageManager mPackageManager();

    @BStaticMethod
    Object createAppContext();

    @BMethod
    Context getReceiverRestrictedContext();

    @BMethod
    void setOuterContext(Context Context0);

    @BMethod
    Object getAttributionSource();

    @BMethod
    PackageManager getPackageManager();
}
