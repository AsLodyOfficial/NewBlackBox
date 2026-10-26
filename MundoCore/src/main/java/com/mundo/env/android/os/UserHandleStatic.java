package com.mundo.env.android.os;

import java.lang.Integer;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.os.UserHandle")
public interface UserHandleStatic {
  @BMethodCheckNotProcess
  Method _check_myUserId();

  Integer myUserId();
}
