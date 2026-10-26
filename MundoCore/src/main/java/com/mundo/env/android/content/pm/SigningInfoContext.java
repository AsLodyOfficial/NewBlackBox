package com.mundo.env.android.content.pm;

import android.content.pm.PackageParser;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.content.pm.SigningInfo")
public interface SigningInfoContext {
  @BFieldSetNotProcess
  void _set_mSigningDetails(final Object value);

  @BFieldCheckNotProcess
  Field _check_mSigningDetails();

  @BFieldNotProcess
  PackageParser.SigningDetails mSigningDetails();
}
