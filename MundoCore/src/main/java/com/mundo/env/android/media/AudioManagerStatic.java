package com.mundo.env.android.media;

import android.content.Context;
import android.os.IInterface;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.media.AudioManager")
public interface AudioManagerStatic {
  @BMethodCheckNotProcess
  Method _check_getService(Context context);

  Object getService(Context context);

  @BMethodCheckNotProcess
  Method _check_isStreamMute(int streamType);

  Boolean isStreamMute(int streamType);

  @BFieldSetNotProcess
  void _set_sService(final Object value);

  @BFieldCheckNotProcess
  Field _check_sService();

  @BFieldNotProcess
  IInterface sService();
}
