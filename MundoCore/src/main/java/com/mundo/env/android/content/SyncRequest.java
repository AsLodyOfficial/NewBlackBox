package com.mundo.env.android.content;

import android.accounts.Account;
import android.os.Bundle;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.content.SyncRequest")
public interface SyncRequest {
    @BField
    Account mAccountToSync();

    @BField
    String mAuthority();

    @BField
    Bundle mExtras();

    @BField
    boolean mIsPeriodic();

    @BField
    long mSyncFlexTimeSecs();

    @BField
    long mSyncRunTimeSecs();
}
