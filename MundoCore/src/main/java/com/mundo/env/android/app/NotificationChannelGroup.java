package com.mundo.env.android.app;

import java.util.List;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.NotificationChannelGroup")
public interface NotificationChannelGroup {
    @BField
    List<android.app.NotificationChannel> mChannels();

    @BField
    String mId();
}
