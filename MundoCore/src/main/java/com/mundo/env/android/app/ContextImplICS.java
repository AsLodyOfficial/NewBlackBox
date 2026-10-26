package com.mundo.env.android.app;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.ContextImpl")
public interface ContextImplICS {
    @BField
    File mExternalCacheDir();

    @BField
    File mExternalFilesDir();
}
