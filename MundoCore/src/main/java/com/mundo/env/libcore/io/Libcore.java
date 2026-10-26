package com.mundo.env.libcore.io;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("libcore.io.Libcore")
public interface Libcore {
    @BStaticField
    Object os();
}
