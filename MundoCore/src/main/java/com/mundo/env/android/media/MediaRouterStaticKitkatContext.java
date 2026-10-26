package com.mundo.env.android.media;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.media.MediaRouter$Static")
public interface MediaRouterStaticKitkatContext {
  @BFieldSetNotProcess
  void _set_mMediaRouterService(final Object value);

  @BFieldCheckNotProcess
  Field _check_mMediaRouterService();

  @BFieldNotProcess
  IInterface mMediaRouterService();
}
