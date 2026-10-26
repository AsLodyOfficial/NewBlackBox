package com.mundo.env.android.content.pm;

import android.content.pm.ServiceInfo;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.pm.PackageParser$Service")
public interface PackageParserServiceContext {
  @BFieldSetNotProcess
  void _set_info(final Object value);

  @BFieldCheckNotProcess
  Field _check_info();

  @BFieldNotProcess
  ServiceInfo info();
}
