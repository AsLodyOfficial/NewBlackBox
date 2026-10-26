package com.mundo.env.android.telephony;

import android.telephony.CellIdentityGsm;
import android.telephony.CellSignalStrengthGsm;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.telephony.CellInfoGsm")
public interface CellInfoGsmContext {
  @BFieldSetNotProcess
  void _set_mCellIdentityGsm(final Object value);

  @BFieldCheckNotProcess
  Field _check_mCellIdentityGsm();

  @BFieldNotProcess
  CellIdentityGsm mCellIdentityGsm();

  @BFieldSetNotProcess
  void _set_mCellSignalStrengthGsm(final Object value);

  @BFieldCheckNotProcess
  Field _check_mCellSignalStrengthGsm();

  @BFieldNotProcess
  CellSignalStrengthGsm mCellSignalStrengthGsm();
}
