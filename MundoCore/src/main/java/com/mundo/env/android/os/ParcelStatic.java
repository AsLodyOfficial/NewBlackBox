package com.mundo.env.android.os;

import java.lang.Integer;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.os.Parcel")
public interface ParcelStatic {
  @BFieldSetNotProcess
  void _set_VAL_PARCELABLE(final Object value);

  @BFieldCheckNotProcess
  Field _check_VAL_PARCELABLE();

  @BFieldNotProcess
  Integer VAL_PARCELABLE();

  @BFieldSetNotProcess
  void _set_VAL_PARCELABLEARRAY(final Object value);

  @BFieldCheckNotProcess
  Field _check_VAL_PARCELABLEARRAY();

  @BFieldNotProcess
  Integer VAL_PARCELABLEARRAY();
}
