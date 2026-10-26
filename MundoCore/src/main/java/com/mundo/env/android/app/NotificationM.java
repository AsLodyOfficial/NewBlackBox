package com.mundo.env.android.app;

import android.graphics.drawable.Icon;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.Notification")
public interface NotificationM {
    @BField
    Icon mLargeIcon();

    @BField
    Icon mSmallIcon();
}
