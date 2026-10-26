package com.mundo.env.android.app;

import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.app.ActivityThread$ProviderClientRecord")
public interface ActivityThreadProviderClientRecordStatic {
  @BConstructorNotProcess
  ActivityThread.ProviderClientRecord _new();
}
