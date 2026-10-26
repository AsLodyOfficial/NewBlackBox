package com.mundo.env.android.telephony;

import android.os.IInterface;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

/**
 * Created by BlackBox on 2022/2/26.
 */
@BClassName("android.telephony.TelephonyManager")
public interface TelephonyManager {

    @BStaticMethod
    Object getSubscriberInfoService();

    @BStaticField
    boolean sServiceHandleCacheEnabled();

    @BStaticField
    IInterface sIPhoneSubInfo();
}
