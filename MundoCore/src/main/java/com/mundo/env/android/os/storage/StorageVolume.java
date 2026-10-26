package com.mundo.env.android.os.storage;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.os.storage.StorageVolume")
public interface StorageVolume {
    @BField
    File mInternalPath();

    @BField
    File mPath();
}
