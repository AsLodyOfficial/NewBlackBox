package com.mundo.env.libcore.io;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("libcore.io.ForwardingOs")
public interface ForwardingOsContext {
  @BFieldSetNotProcess
  void _set_os(final Object value);

  @BFieldCheckNotProcess
  Field _check_os();

  @BFieldNotProcess
  Object os();
}
