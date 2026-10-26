package com.mundo.env.com.cosmos.apm.framework.page;

import android.app.Instrumentation;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("com.cosmos.apm.framework.page.ActivityLifeCycleHelper$ApplicationInstrumentation")
public interface ActivityLifeCycleHelper$ApplicationInstrumentationContext {
  @BFieldSetNotProcess
  void _set_mBase(final Object value);

  @BFieldCheckNotProcess
  Field _check_mBase();

  @BFieldNotProcess
  Instrumentation mBase();
}
