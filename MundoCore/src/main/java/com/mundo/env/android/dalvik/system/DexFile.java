package com.mundo.env.android.dalvik.system;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BMethod;
import com.mundo.reflection.annotation.BStaticField;

@BClassName("dalvik.system.DexFile")
public interface DexFile {

    @BMethod
    Object openDexFileNative(String sourceName, String outputName, int flags);

    @BMethod
    Object openDexFile(String sourceName, String outputName, int flags);

    @BMethod
    Object loadDex(String sourceName, String outputName, int flags);

    @BStaticField
    Object mCookie();
}
