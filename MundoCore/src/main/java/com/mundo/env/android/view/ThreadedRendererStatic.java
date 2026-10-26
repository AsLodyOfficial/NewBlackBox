package com.mundo.env.android.view;

import java.io.File;
import java.lang.Void;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.view.ThreadedRenderer")
public interface ThreadedRendererStatic {
  @BMethodCheckNotProcess
  Method _check_setupDiskCache(File File0);

  Void setupDiskCache(File File0);
}
