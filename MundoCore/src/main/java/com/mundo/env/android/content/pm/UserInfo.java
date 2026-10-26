package com.mundo.env.android.content.pm;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.content.pm.UserInfo")
public interface UserInfo {
    @BConstructor
    Object _new(int id, String name, int flags);

    @BStaticField
    int FLAG_PRIMARY();
}
