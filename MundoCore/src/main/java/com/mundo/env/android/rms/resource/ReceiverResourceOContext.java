package com.mundo.env.android.rms.resource;

import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.rms.resource.ReceiverResource")
public interface ReceiverResourceOContext {
  @BFieldSetNotProcess
  void _set_mWhiteListMap(final Object value);

  @BFieldCheckNotProcess
  Field _check_mWhiteListMap();

  @BFieldNotProcess
  Map<Integer, List<String>> mWhiteListMap();
}
