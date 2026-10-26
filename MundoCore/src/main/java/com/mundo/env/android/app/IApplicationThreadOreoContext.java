package com.mundo.env.android.app;

import java.lang.Void;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.app.IApplicationThread")
public interface IApplicationThreadOreoContext {
  @BMethodCheckNotProcess
  Method _check_scheduleServiceArgs();

  Void scheduleServiceArgs();
}
