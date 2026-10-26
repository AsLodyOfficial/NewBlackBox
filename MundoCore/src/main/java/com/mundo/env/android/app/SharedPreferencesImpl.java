package com.mundo.env.android.app;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;

@BClassName("android.app.SharedPreferencesImpl")
public interface SharedPreferencesImpl {
    @BConstructor
    SharedPreferencesImpl _new(File File0, int int1);
}
