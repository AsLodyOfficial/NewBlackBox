package com.mundo.env.android.app;

import android.content.pm.ProviderInfo;
import android.os.IInterface;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.app.IActivityManager$ContentProviderHolder")
public interface IActivityManagerContentProviderHolderContext {
  @BFieldSetNotProcess
  void _set_info(final Object value);

  @BFieldCheckNotProcess
  Field _check_info();

  @BFieldNotProcess
  ProviderInfo info();

  @BFieldSetNotProcess
  void _set_noReleaseNeeded(final Object value);

  @BFieldCheckNotProcess
  Field _check_noReleaseNeeded();

  @BFieldNotProcess
  Boolean noReleaseNeeded();

  @BFieldSetNotProcess
  void _set_provider(final Object value);

  @BFieldCheckNotProcess
  Field _check_provider();

  @BFieldNotProcess
  IInterface provider();
}
