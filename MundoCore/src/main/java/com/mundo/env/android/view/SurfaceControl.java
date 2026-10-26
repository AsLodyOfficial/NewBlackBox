package com.mundo.env.android.view;

import android.graphics.Bitmap;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.view.SurfaceControl")
public interface SurfaceControl {
    @BStaticMethod
    Bitmap screnshot(int int0, int int1);
}
