package com.mundo.env.android.view;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.view.DisplayAdjustments")
public interface DisplayAdjustments {
    @BMethod
    void setCompatibilityInfo();
}
