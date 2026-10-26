package com.mundo.env.android.app;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.app.ActivityTaskManager")
public interface ActivityTaskManager {

    @BStaticMethod
    Object getService();

    @BStaticField
    Object IActivityTaskManagerSingleton();
}
