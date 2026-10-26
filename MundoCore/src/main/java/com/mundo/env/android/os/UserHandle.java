package com.mundo.env.android.os;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.os.UserHandle")
public interface UserHandle {
    @BStaticMethod
    Integer myUserId();
}
