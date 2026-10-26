package com.mundo.env.android.telephony;

import java.lang.Integer;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.telephony.CellIdentityGsm")
public interface CellIdentityGsmContext {
  @BFieldSetNotProcess
  void _set_mCid(final Object value);

  @BFieldCheckNotProcess
  Field _check_mCid();

  @BFieldNotProcess
  Integer mCid();

  @BFieldSetNotProcess
  void _set_mLac(final Object value);

  @BFieldCheckNotProcess
  Field _check_mLac();

  @BFieldNotProcess
  Integer mLac();

  @BFieldSetNotProcess
  void _set_mMcc(final Object value);

  @BFieldCheckNotProcess
  Field _check_mMcc();

  @BFieldNotProcess
  Integer mMcc();

  @BFieldSetNotProcess
  void _set_mMnc(final Object value);

  @BFieldCheckNotProcess
  Field _check_mMnc();

  @BFieldNotProcess
  Integer mMnc();
}
