package com.mundo.env.android.content;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BMethod;

/**
 * Created by BlackBox on 2022/2/20.
 */
@BClassName("android.content.AttributionSource")
public interface AttributionSource {
    @BConstructor
    Object _new(int i,int i2,String str,String str2);

    @BConstructor
    Object _new(int i,String str,String str2);

    @BField
    Object mAttributionSourceState();

    @BMethod
    Object getNext();
}
