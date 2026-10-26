package com.mundo.env.android.app;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.NotificationChannel")
public interface NotificationChannel {
    @BField
    String mId();
}
