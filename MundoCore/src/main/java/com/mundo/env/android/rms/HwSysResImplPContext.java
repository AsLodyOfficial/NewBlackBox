package com.mundo.env.android.rms;

import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.rms.HwSysResImpl")
public interface HwSysResImplPContext {
  @BFieldSetNotProcess
  void _set_mWhiteListMap(final Object value);

  @BFieldCheckNotProcess
  Field _check_mWhiteListMap();

  @BFieldNotProcess
  Map<Integer, ArrayList<String>> mWhiteListMap();
}
