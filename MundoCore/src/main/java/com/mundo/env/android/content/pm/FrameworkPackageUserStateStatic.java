package com.mundo.env.android.content.pm;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.pm.pkg.FrameworkPackageUserState")
public interface FrameworkPackageUserStateStatic {
  @BFieldSetNotProcess
  void _set_gDefault(final Object value);

  @BFieldCheckNotProcess
  Field _check_gDefault();

  @BFieldNotProcess
  Object gDefault();
}
