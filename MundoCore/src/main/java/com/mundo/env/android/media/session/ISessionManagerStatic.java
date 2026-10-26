package com.mundo.env.android.media.session;

import android.content.Context;
import android.os.IInterface;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.media.session.ISessionManager")
public interface ISessionManagerStatic {
  @BMethodCheckNotProcess
  Method _check_getService(Context context);

  IInterface getService(Context context);
}
