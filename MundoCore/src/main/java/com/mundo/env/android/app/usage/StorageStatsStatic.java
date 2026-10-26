package com.mundo.env.android.app.usage;

import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.app.usage.StorageStats")
public interface StorageStatsStatic {
  @BConstructorNotProcess
  StorageStats _new();
}
