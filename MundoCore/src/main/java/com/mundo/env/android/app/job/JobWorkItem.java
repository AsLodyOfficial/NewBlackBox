package com.mundo.env.android.app.job;

import android.content.Intent;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BField;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.app.job.JobWorkItem")
public interface JobWorkItem {
    @BConstructor
    JobWorkItem _new(Intent Intent0);

    @BField
    int mDeliveryCount();

    @BField
    Object mGrants();

    @BField
    int mWorkId();

    @BMethod
    Intent getIntent();
}
