package com.mundo.env.android.os;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.os.Message")
public interface Message {
    @BStaticMethod
    void updateCheckRecycle(int int0);
}
