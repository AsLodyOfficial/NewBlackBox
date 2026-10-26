package com.mundo.env.android.os;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.os.Process")
public interface Process {
    @BStaticMethod
    void setArgV0(String String0);
}
