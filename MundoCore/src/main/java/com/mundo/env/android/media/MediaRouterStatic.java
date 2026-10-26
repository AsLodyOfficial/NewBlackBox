package com.mundo.env.android.media;

import android.content.Context;
import java.lang.Object;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.media.MediaRouter")
public interface MediaRouterStatic {
  @BMethodCheckNotProcess
  Method _check_getInstance(Context context);

  Object getInstance(Context context);

  @BFieldSetNotProcess
  void _set_sStatic(final Object value);

  @BFieldCheckNotProcess
  Field _check_sStatic();

  @BFieldNotProcess
  Object sStatic();
}
