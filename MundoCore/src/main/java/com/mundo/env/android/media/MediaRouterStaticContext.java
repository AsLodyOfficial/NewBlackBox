package com.mundo.env.android.media;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.media.MediaRouter$Static")
public interface MediaRouterStaticContext {
  @BFieldSetNotProcess
  void _set_mAudioService(final Object value);

  @BFieldCheckNotProcess
  Field _check_mAudioService();

  @BFieldNotProcess
  IInterface mAudioService();
}
