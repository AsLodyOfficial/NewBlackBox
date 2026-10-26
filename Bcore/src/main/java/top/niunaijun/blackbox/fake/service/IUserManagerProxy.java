package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;

import black.android.content.pm.BRUserInfo;
import black.android.os.BRIUserManagerStub;
import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;


public class IUserManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IUserManagerProxy";

    public IUserManagerProxy() {
        super(BRServiceManager.get().getService(Context.USER_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIUserManagerStub.get().asInterface(BRServiceManager.get().getService(Context.USER_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.USER_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if ("getApplicationRestrictionsForUser".equals(name) || "getApplicationRestrictions".equals(name)) {
            Log.d(TAG, "Intercepted " + name + ", returning empty Bundle");
            return new Bundle();
        }
        if ("getUserRestrictions".equals(name) || "getDefaultUserRestrictions".equals(name)) {
            return new Bundle();
        }
        if ("hasUserRestriction".equals(name) || "hasUserRestrictionForUser".equals(name) || "hasBaseUserRestriction".equals(name)) {
            return false;
        }
        try {
            return super.invoke(proxy, method, args);
        } catch (SecurityException e) {
            Log.w(TAG, "IUserManager SecurityException in " + name + ", returning safe default", e);
            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class || returnType == Boolean.class) {
                return false;
            } else if (returnType == int.class || returnType == Integer.class) {
                return 0;
            } else if (returnType == Bundle.class) {
                return new Bundle();
            }
            return null;
        }
    }

    @ProxyMethod("getApplicationRestrictions")
    public static class GetApplicationRestrictions extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return new Bundle();
        }
    }

    @ProxyMethod("getApplicationRestrictionsForUser")
    public static class GetApplicationRestrictionsForUser extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return new Bundle();
        }
    }

    @ProxyMethod("getProfileParent")
    public static class GetProfileParent extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object blackBox = BRUserInfo.get()._new(BActivityThread.getUserId(), "BlackBox", BRUserInfo.get().FLAG_PRIMARY());
            return blackBox;
        }
    }

    @ProxyMethod("getUsers")
    public static class getUsers extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return new ArrayList<>();
        }
    }
}
