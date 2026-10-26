package com.mundo.env.android.content;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.content.PeriodicSync")
public interface PeriodicSync {
    @BField
    long flexTime();
}
