package com.mundo.env.android.app.job;

import android.content.Intent;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.app.job.JobWorkItem")
public interface JobWorkItemStatic {
  @BConstructorNotProcess
  JobWorkItem _new(Intent Intent0);
}
