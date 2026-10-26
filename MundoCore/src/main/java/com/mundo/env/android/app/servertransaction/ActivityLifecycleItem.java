package com.mundo.env.android.app.servertransaction;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.app.servertransaction.ActivityLifecycleItem")
public interface ActivityLifecycleItem {
    @BMethod
    Integer getTargetState();
}
