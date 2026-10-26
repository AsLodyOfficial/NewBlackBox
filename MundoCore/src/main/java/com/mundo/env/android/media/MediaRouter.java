package com.mundo.env.android.media;

import android.content.Context;
import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.media.MediaRouter")
public interface MediaRouter {
    @BStaticField
    Object sStatic();

    @BClassName("android.media.MediaRouter$Static")
    interface StaticKitkat {
        @BField
        IInterface mMediaRouterService();
    }

    @BClassName("android.media.MediaRouter$Static")
    interface Static {
        @BField
        IInterface mAudioService();
    }

    // Add a method to get the MediaRouter instance dynamically for Android 10+
    @BStaticMethod
    Object getInstance(Context context);
}
