package com.mundo.env.android.content.pm;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.content.pm.ApplicationInfo")
public interface ApplicationInfoP {
    @BField
    String[] splitClassLoaderNames();

    @BMethod
    void setHiddenApiEnforcementPolicy(int int0);
}
