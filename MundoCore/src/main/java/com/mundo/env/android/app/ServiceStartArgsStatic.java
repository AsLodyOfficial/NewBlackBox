package com.mundo.env.android.app;

import android.content.Intent;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.app.ServiceStartArgs")
public interface ServiceStartArgsStatic {
  @BConstructorNotProcess
  ServiceStartArgs _new(boolean boolean0, int int1, int int2, Intent Intent3);
}
