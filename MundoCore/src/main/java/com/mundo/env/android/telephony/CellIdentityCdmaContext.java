package com.mundo.env.android.telephony;

import java.lang.Integer;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.telephony.CellIdentityCdma")
public interface CellIdentityCdmaContext {
  @BFieldSetNotProcess
  void _set_mBasestationId(final Object value);

  @BFieldCheckNotProcess
  Field _check_mBasestationId();

  @BFieldNotProcess
  Integer mBasestationId();

  @BFieldSetNotProcess
  void _set_mNetworkId(final Object value);

  @BFieldCheckNotProcess
  Field _check_mNetworkId();

  @BFieldNotProcess
  Integer mNetworkId();

  @BFieldSetNotProcess
  void _set_mSystemId(final Object value);

  @BFieldCheckNotProcess
  Field _check_mSystemId();

  @BFieldNotProcess
  Integer mSystemId();
}
