package com.mundo.env.android.telephony;

import java.lang.Integer;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.telephony.CellSignalStrengthGsm")
public interface CellSignalStrengthGsmContext {
  @BFieldSetNotProcess
  void _set_mBitErrorRate(final Object value);

  @BFieldCheckNotProcess
  Field _check_mBitErrorRate();

  @BFieldNotProcess
  Integer mBitErrorRate();

  @BFieldSetNotProcess
  void _set_mSignalStrength(final Object value);

  @BFieldCheckNotProcess
  Field _check_mSignalStrength();

  @BFieldNotProcess
  Integer mSignalStrength();
}
