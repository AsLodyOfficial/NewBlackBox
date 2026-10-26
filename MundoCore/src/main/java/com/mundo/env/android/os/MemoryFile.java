package com.mundo.env.android.os;

import java.io.FileDescriptor;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.os.MemoryFile")
public interface MemoryFile {
    @BMethod
    FileDescriptor getFileDescriptor();
}
