package com.mundo.env.android.os;

import java.lang.Void;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.os.Message")
public interface MessageStatic {
  @BMethodCheckNotProcess
  Method _check_updateCheckRecycle(int int0);

  Void updateCheckRecycle(int int0);
}
