package com.mundo.env.java.io;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("java.io.File")
public interface File {
    @BStaticField
    Object fs();
}
