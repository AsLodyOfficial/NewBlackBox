package com.mundo.env.android.content;

import android.accounts.Account;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;

@BClassName("android.content.SyncInfo")
public interface SyncInfo {
    @BConstructor
    SyncInfo _new(int int0, Account Account1, String String2, long long3);
}
