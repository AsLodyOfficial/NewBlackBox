package com.mundo.env.android.util;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.util.Singleton")
public interface Singleton {
    @BField
    Object mInstance();

    @BMethod
    Object get();
}
