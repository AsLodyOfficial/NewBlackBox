package com.mundo.env.android.dalvik.system;

import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("dalvik.system.DexFile")
public interface DexFileStatic {
  @BFieldSetNotProcess
  void _set_mCookie(final Object value);

  @BFieldCheckNotProcess
  Field _check_mCookie();

  @BFieldNotProcess
  Object mCookie();
}
