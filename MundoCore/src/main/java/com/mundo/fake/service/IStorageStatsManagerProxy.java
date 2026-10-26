package com.mundo.fake.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;

import com.mundo.env.android.app.usage.BRIStorageStatsManagerStub;
import com.mundo.env.android.os.BRServiceManager;

import com.mundo.fake.hook.BinderInvocationStub;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.utils.MethodParameterUtils;
import com.mundo.utils.Slog;

/**
 * StorageStats compatibility proxy
 * Android 8 → Android 17
 * PUBG / BGMI update & permission fix
 */
@TargetApi(Build.VERSION_CODES.O)
public class IStorageStatsManagerProxy extends BinderInvocationStub {

    public IStorageStatsManagerProxy() {
        super(BRServiceManager.get().getService(Context.STORAGE_STATS_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIStorageStatsManagerStub.get().asInterface(BRServiceManager.get().getService(Context.STORAGE_STATS_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.STORAGE_STATS_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }

    @ProxyMethod("getFreeBytes")
    public static class GetFreeBytes extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 68719476736L; // 64 GB
        }
    }

    @ProxyMethod("getTotalBytes")
    public static class GetTotalBytes extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 107374182400L; // 100 GB
        }
    }
}
