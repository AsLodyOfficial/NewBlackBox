package com.mundo.env.android.widget;

import android.content.pm.ApplicationInfo;
import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Field;
import java.util.ArrayList;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BFieldCheckNotProcess;
import com.mundo.reflection.annotation.BFieldNotProcess;
import com.mundo.reflection.annotation.BFieldSetNotProcess;

@BClassNameNotProcess("android.widget.RemoteViews")
public interface RemoteViewsContext {
  @BFieldSetNotProcess
  void _set_mActions(final Object value);

  @BFieldCheckNotProcess
  Field _check_mActions();

  @BFieldNotProcess
  ArrayList<Object> mActions();

  @BFieldSetNotProcess
  void _set_mApplication(final Object value);

  @BFieldCheckNotProcess
  Field _check_mApplication();

  @BFieldNotProcess
  ApplicationInfo mApplication();

  @BFieldSetNotProcess
  void _set_mPackage(final Object value);

  @BFieldCheckNotProcess
  Field _check_mPackage();

  @BFieldNotProcess
  String mPackage();
}
