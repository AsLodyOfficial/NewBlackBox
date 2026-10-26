package com.mundo.fake.service.libcore;

import android.os.Process;

import java.lang.reflect.Method;

import com.mundo.env.libcore.io.BRLibcore;
import java.util.Objects;
import com.mundo.MundoCore;
import com.mundo.app.BActivityThread;
import com.mundo.core.VCore;
import com.mundo.fake.hook.ClassInvocationStub;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.fake.hook.ProxyMethods;
import com.mundo.utils.Reflector;
import com.mundo.utils.Slog;

/**
 * Created by @officialakshit01 on 4/9/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶 つ０
 * しーＪ
 * 此处无Bug
 */
public class OsStub extends ClassInvocationStub {
    public static final String TAG = "OsStub";
    private final Object mBase;

    public OsStub() {
        mBase = BRLibcore.get().os();
    }

    @Override
    protected Object getWho() {
        return mBase;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        BRLibcore.get()._set_os(proxyInvocation);
    }

    @Override
    protected void onBindMethod() {
    }

    @Override
    public boolean isBadEnv() {
        return BRLibcore.get().os() != getProxyInvocation();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null)
                    continue;
                if (args[i] instanceof String && ((String) args[i]).startsWith("/")) {
                    String orig = (String) args[i];
                    args[i] = VCore.get().redirectPath(orig);
                }
            }
        }
        return super.invoke(proxy, method, args);
    }

    @ProxyMethod("getuid")
    public static class getuid extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int callUid = (int) method.invoke(who, args);
            return getFakeUid(callUid);
        }
    }

    @ProxyMethod("stat")
    public static class stat extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object invoke = null;
            try {
                invoke = method.invoke(who, args);
            } catch (Throwable e) {
                throw e.getCause();
            }
            Reflector.with(invoke).field("st_uid").set(getFakeUid(-1));
            return invoke;
        }
    }

    @ProxyMethod("statvfs")
    public static class statvfs extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                String path = (String) args[0];
                if (path.contains("freefire") || path.contains("dts") || path.contains("SdCard") ||
                    path.contains("storage") || path.contains("sdcard") || path.contains("contentcache")) {
                    return createFakeStatVfs();
                }
            }
            try {
                Object res = method.invoke(who, args);
                if (res instanceof android.system.StructStatVfs) {
                    android.system.StructStatVfs vfs = (android.system.StructStatVfs) res;
                    long frsize = vfs.f_frsize > 0 ? vfs.f_frsize : (vfs.f_bsize > 0 ? vfs.f_bsize : 4096);
                    if (vfs.f_bavail <= 0 || vfs.f_bavail * frsize < 1073741824L) {
                        return createFakeStatVfs();
                    }
                    return res;
                }
            } catch (Throwable ignored) {}
            return createFakeStatVfs();
        }
    }

    @ProxyMethod("fstatvfs")
    public static class fstatvfs extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Object res = method.invoke(who, args);
                if (res instanceof android.system.StructStatVfs) {
                    android.system.StructStatVfs vfs = (android.system.StructStatVfs) res;
                    long frsize = vfs.f_frsize > 0 ? vfs.f_frsize : (vfs.f_bsize > 0 ? vfs.f_bsize : 4096);
                    if (vfs.f_bavail <= 0 || vfs.f_bavail * frsize < 1073741824L) {
                        return createFakeStatVfs();
                    }
                    return res;
                }
            } catch (Throwable ignored) {}
            return createFakeStatVfs();
        }
    }

    private static Object createFakeStatVfs() {
        try {
            return new android.system.StructStatVfs(
                4096L,       // f_bsize
                4096L,       // f_frsize
                26214400L,   // f_blocks (~100 GB)
                16777216L,   // f_bfree  (~64 GB)
                16777216L,   // f_bavail (~64 GB)
                1000000L,    // f_files
                500000L,     // f_ffree
                500000L,     // f_favail
                0L,          // f_fsid
                0L,          // f_flag
                255L         // f_namemax
            );
        } catch (Throwable t) {
            return null;
        }
    }

    private static int getFakeUid(int callUid) {
        if (callUid > 0 && callUid <= Process.FIRST_APPLICATION_UID)
            return callUid;
        if (BActivityThread.isThreadInit() && BActivityThread.currentActivityThread().isInit()) {
            return BActivityThread.getBAppId();
        } else {
            return MundoCore.getHostUid();
        }
    }
}
