package com.mundo.env.android.os;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.os.Environment")
public interface EnvironmentStatic {
  @BFieldSetNotProcess
  void _set_sCurrentUser(final Object value);

  @BFieldCheckNotProcess
  Field _check_sCurrentUser();

  @BFieldNotProcess
  Object sCurrentUser();
}
