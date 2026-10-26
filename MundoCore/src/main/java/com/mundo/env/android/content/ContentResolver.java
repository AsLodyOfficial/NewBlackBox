package com.mundo.env.android.content;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.content.ContentResolver")
public interface ContentResolver {
    @BStaticField
    IInterface sContentService();

    @BField
    String mPackageName();
}
