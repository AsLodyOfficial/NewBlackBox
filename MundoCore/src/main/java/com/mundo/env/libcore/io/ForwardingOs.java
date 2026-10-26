package com.mundo.env.libcore.io;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("libcore.io.ForwardingOs")
public interface ForwardingOs {
    @BField
    Object os();
}
