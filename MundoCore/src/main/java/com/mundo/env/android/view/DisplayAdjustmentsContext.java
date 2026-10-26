package com.mundo.env.android.view;

import java.lang.Void;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.view.DisplayAdjustments")
public interface DisplayAdjustmentsContext {
  @BMethodCheckNotProcess
  Method _check_setCompatibilityInfo();

  Void setCompatibilityInfo();
}
