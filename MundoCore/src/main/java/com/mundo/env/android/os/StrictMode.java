package com.mundo.env.android.os;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.os.StrictMode")
public interface StrictMode {
    @BStaticField
    int DETECT_VM_FILE_URI_EXPOSURE();

    @BStaticField
    int PENALTY_DEATH_ON_FILE_URI_EXPOSURE();

    @BStaticField
    int sVmPolicyMask();

    @BStaticMethod
    void disableDeathOnFileUriExposure();
}
