package com.mundo.env.android.app;

import android.app.PendingIntent;
import android.content.Context;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.app.Notification")
public interface Notification {
    @BMethod
    void setLatestEventInfo(Context Context0, CharSequence CharSequence1, CharSequence CharSequence2, PendingIntent PendingIntent3);
}
