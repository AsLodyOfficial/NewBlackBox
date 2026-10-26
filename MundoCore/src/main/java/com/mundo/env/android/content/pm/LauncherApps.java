package com.mundo.env.android.content.pm;

import android.content.pm.PackageManager;
import android.os.IInterface;
import android.os.UserManager;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.content.pm.LauncherApps")
public interface LauncherApps {
    @BField
    PackageManager mPm();

    @BField
    IInterface mService();

    @BField
    UserManager mUserManager();
}
