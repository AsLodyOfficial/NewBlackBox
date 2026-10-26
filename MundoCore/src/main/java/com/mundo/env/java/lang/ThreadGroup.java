package com.mundo.env.java.lang;

import java.util.List;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("java.lang.ThreadGroup")
public interface ThreadGroup {
    @BField
    List<java.lang.ThreadGroup> groups();

    @BField
    java.lang.ThreadGroup parent();
}
