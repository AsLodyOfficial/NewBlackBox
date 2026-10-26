package com.mundo.env.android.app.servertransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.servertransaction.LaunchActivityItem")
public interface LaunchActivityItem {
    @BField
    ActivityInfo mInfo();

    @BField
    Intent mIntent();
}
