package com.mundo.env.android.net.wifi;

import android.os.IInterface;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.net.wifi.WifiManager")
public interface WifiManagerStatic {
  @BFieldSetNotProcess
  void _set_sService(final Object value);

  @BFieldCheckNotProcess
  Field _check_sService();

  @BFieldNotProcess
  IInterface sService();
}
