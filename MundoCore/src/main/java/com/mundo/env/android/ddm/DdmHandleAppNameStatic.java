package com.mundo.env.android.ddm;

import java.lang.String;
import java.lang.Void;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.ddm.DdmHandleAppName")
public interface DdmHandleAppNameStatic {
  @BMethodCheckNotProcess
  Method _check_setAppName(String String0, int i);

  Void setAppName(String String0, int i);
}
