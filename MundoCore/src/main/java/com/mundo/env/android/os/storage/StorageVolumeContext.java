package com.mundo.env.android.os.storage;

import java.io.File;
import java.lang.Object;
import java.lang.reflect.Field;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.os.storage.StorageVolume")
public interface StorageVolumeContext {
  @BFieldSetNotProcess
  void _set_mInternalPath(final Object value);

  @BFieldCheckNotProcess
  Field _check_mInternalPath();

  @BFieldNotProcess
  File mInternalPath();

  @BFieldSetNotProcess
  void _set_mPath(final Object value);

  @BFieldCheckNotProcess
  Field _check_mPath();

  @BFieldNotProcess
  File mPath();
}
