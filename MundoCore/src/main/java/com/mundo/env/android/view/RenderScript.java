package com.mundo.env.android.view;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.renderscript.RenderScript")
public interface RenderScript {
    @BStaticMethod
    void setupDiskCache(File File0);
}
