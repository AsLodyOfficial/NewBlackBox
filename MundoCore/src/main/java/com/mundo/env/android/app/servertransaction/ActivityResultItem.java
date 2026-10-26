package com.mundo.env.android.app.servertransaction;

import java.util.List;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.servertransaction.ActivityResultItem")
public interface ActivityResultItem {
    @BField
    List mResultInfoList();
}
