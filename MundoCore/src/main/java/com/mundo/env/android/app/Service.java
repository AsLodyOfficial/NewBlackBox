package com.mundo.env.android.app;


import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.IBinder;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;
import com.mundo.reflection.annotation.BParamClass;

@BClassName("android.app.Service")
public interface Service {
    @BMethod
    void attach(Context context,
                @BParamClass(ActivityThread.class) Object thread, String className, IBinder token,
                Application application, Object activityManager);
}
