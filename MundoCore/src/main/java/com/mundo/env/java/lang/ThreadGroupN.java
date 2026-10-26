package com.mundo.env.java.lang;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("java.lang.ThreadGroup")
public interface ThreadGroupN {
    @BField
    java.lang.ThreadGroup[] groups();

    @BField
    Integer ngroups();

    @BField
    java.lang.ThreadGroup parent();
}
