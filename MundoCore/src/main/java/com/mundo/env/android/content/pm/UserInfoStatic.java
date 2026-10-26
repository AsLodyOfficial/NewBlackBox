package com.mundo.env.android.content.pm;

import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.pm.UserInfo")
public interface UserInfoStatic {
  @BFieldSetNotProcess
  void _set_FLAG_PRIMARY(final Object value);

  @BFieldCheckNotProcess
  Field _check_FLAG_PRIMARY();

  @BFieldNotProcess
  Integer FLAG_PRIMARY();

  @BConstructorNotProcess
  Object _new(int id, String name, int flags);
}
