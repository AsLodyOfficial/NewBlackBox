package com.mundo.env.android.rms.resource;

import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import java.util.List;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.rms.resource.ReceiverResource")
public interface ReceiverResourceNContext {
  @BFieldSetNotProcess
  void _set_mWhiteList(final Object value);

  @BFieldCheckNotProcess
  Field _check_mWhiteList();

  @BFieldNotProcess
  List<String> mWhiteList();
}
