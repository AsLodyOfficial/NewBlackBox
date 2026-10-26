package com.mundo.env.android.view;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.view.HardwareRenderer")
public interface HardwareRenderer {
    @BStaticMethod
    void setupDiskCache(File File0);
}
