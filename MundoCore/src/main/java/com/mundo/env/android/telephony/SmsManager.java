package com.mundo.env.android.telephony;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.telephony.SmsManager")
public interface SmsManager {
    @BMethod
    Boolean getAutoPersisting();

    @BMethod
    void setAutoPersisting(boolean boolean0);
}
