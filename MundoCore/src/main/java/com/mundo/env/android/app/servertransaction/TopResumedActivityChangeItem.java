package com.mundo.env.android.app.servertransaction;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.servertransaction.TopResumedActivityChangeItem")
public interface TopResumedActivityChangeItem {
    @BField
    Boolean mOnTop();
}
