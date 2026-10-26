package com.mundo.env.android.os;

import java.io.FileDescriptor;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;

@BClassNameNotProcess("android.os.MemoryFile")
public interface MemoryFileContext {
  @BMethodCheckNotProcess
  Method _check_getFileDescriptor();

  FileDescriptor getFileDescriptor();
}
