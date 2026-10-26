package com.mundo.env.android.app.servertransaction;

import java.lang.Object;
import java.lang.reflect.Field;
import java.util.List;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.servertransaction.ActivityResultItem")
public interface ActivityResultItemContext {
  @BFieldSetNotProcess
  void _set_mResultInfoList(final Object value);

  @BFieldCheckNotProcess
  Field _check_mResultInfoList();

  @BFieldNotProcess
  List mResultInfoList();
}
