package com.mundo.env.android.app.job;

import android.content.ComponentName;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.job.JobInfo")
public interface JobInfo {
    @BField
    long flexMillis();

    @BField
    long intervalMillis();

    @BField
    int jobId();

    @BField
    ComponentName service();
}
