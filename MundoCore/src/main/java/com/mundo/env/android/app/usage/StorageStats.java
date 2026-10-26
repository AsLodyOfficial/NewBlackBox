package com.mundo.env.android.app.usage;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.usage.StorageStats")
public interface StorageStats {
    @BConstructor
    StorageStats _new();

    @BField
    long cacheBytes();

    @BField
    long codeBytes();

    @BField
    long dataBytes();
}
