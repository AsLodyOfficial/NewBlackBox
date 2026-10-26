package com.mundo.env.android.app;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.app.WallpaperManager")
public interface WallpaperManager {
    @BStaticField
    Object sGlobals();

    @BClassName("android.app.WallpaperManager$Globals")
    interface Globals {
        @BField
        Object mService();
    }
}
