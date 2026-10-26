package com.mundo.env.java.io;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("java.io.File")
public interface FileStatic {
  @BFieldSetNotProcess
  void _set_fs(final Object value);

  @BFieldCheckNotProcess
  Field _check_fs();

  @BFieldNotProcess
  Object fs();
}
