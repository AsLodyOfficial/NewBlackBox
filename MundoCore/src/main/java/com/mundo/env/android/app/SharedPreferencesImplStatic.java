package com.mundo.env.android.app;

import java.io.File;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.app.SharedPreferencesImpl")
public interface SharedPreferencesImplStatic {
  @BConstructorNotProcess
  SharedPreferencesImpl _new(File File0, int int1);
}
