package com.mundo.env.android.app.servertransaction;

import android.os.IBinder;
import java.lang.Object;
import java.lang.reflect.Field;
import java.util.List;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.servertransaction.ClientTransaction")
public interface ClientTransactionContext {
  @BFieldSetNotProcess
  void _set_mActivityCallbacks(final Object value);

  @BFieldCheckNotProcess
  Field _check_mActivityCallbacks();

  @BFieldNotProcess
  List<Object> mActivityCallbacks();

  @BFieldSetNotProcess
  void _set_mActivityToken(final Object value);

  @BFieldCheckNotProcess
  Field _check_mActivityToken();

  @BFieldNotProcess
  IBinder mActivityToken();

  @BFieldSetNotProcess
  void _set_mLifecycleStateRequest(final Object value);

  @BFieldCheckNotProcess
  Field _check_mLifecycleStateRequest();

  @BFieldNotProcess
  Object mLifecycleStateRequest();
}
