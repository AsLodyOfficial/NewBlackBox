package com.mundo.env.android.content;

import java.util.List;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.content.IntentFilter")
public interface IntentFilter {
    @BField
    List<String> mActions();

    @BField
    List<String> mCategories();
}
