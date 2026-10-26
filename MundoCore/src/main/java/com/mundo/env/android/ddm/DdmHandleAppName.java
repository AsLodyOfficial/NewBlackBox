package com.mundo.env.android.ddm;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.ddm.DdmHandleAppName")
public interface DdmHandleAppName {
    @BStaticMethod
    void setAppName(String String0, int i);
}
