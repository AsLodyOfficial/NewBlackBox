package com.mundo.env.com.android.internal;

import java.lang.Integer;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("com.android.internal.R$layout")
public interface RlayoutStatic {
  @BFieldSetNotProcess
  void _set_resolver_list(final Object value);

  @BFieldCheckNotProcess
  Field _check_resolver_list();

  @BFieldNotProcess
  Integer resolver_list();
}
