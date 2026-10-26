package com.mundo.env.android.app;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.Notification")
public interface NotificationO {
    @BField
    String mChannelId();

    @BField
    String mGroupKey();
}
