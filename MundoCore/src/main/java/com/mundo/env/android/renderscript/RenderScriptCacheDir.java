package com.mundo.env.android.renderscript;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.renderscript.RenderScriptCacheDir")
public interface RenderScriptCacheDir {
    @BStaticMethod
    void setupDiskCache(File File0);
}
