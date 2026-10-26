package com.mundo.env.android.os;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("android.os.Environment")
public interface Environment {

  @BStaticField
  Object sCurrentUser();
}