package com.mundo.env.android.os.storage;

import android.os.storage.StorageVolume;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.os.storage.StorageManager")
public interface StorageManager {
    @BStaticMethod
    StorageVolume[] getVolumeList(int int0, int int1);
}
