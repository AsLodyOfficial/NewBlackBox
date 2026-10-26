package com.mundo.env.android.content;

import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.ContentResolver")
public interface ContentResolverContext {
  @BFieldSetNotProcess
  void _set_mPackageName(final Object value);

  @BFieldCheckNotProcess
  Field _check_mPackageName();

  @BFieldNotProcess
  String mPackageName();
}
