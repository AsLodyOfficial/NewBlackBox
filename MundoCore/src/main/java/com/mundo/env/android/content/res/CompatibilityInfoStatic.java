package com.mundo.env.android.content.res;

import android.content.pm.ApplicationInfo;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.res.CompatibilityInfo")
public interface CompatibilityInfoStatic {
  @BFieldSetNotProcess
  void _set_DEFAULT_COMPATIBILITY_INFO(final Object value);

  @BFieldCheckNotProcess
  Field _check_DEFAULT_COMPATIBILITY_INFO();

  @BFieldNotProcess
  Object DEFAULT_COMPATIBILITY_INFO();

  @BConstructorNotProcess
  CompatibilityInfo _new(ApplicationInfo ApplicationInfo0, int int1, int int2, boolean boolean3);

  @BConstructorNotProcess
  CompatibilityInfo _new(ApplicationInfo ApplicationInfo0, int int1, int int2, boolean boolean3,
      int int4);
}
