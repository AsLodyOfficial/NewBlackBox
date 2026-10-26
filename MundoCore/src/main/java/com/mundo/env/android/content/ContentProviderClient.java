package com.mundo.env.android.content;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.content.ContentProviderClient")
public interface ContentProviderClient {
    @BField
    IInterface mContentProvider();
}
