package com.mundo.env.android.app.job;

import android.os.IBinder;
import android.os.PersistableBundle;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

@BClassName("android.app.job.JobParameters")
public interface JobParameters {
    @BField
    IBinder callback();

    @BField
    PersistableBundle extras();

    @BField
    int jobId();
}
