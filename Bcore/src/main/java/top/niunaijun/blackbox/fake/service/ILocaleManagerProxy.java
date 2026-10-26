package top.niunaijun.blackbox.fake.service;

import android.os.IBinder;
import android.os.LocaleList;
import android.util.Log;

import java.lang.reflect.Method;

import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;

public class ILocaleManagerProxy extends BinderInvocationStub {
    public static final String TAG = "ILocaleManagerProxy";

    public ILocaleManagerProxy() {
        super(BRServiceManager.get().getService("locale"));
    }

    @Override
    protected Object getWho() {
        try {
            IBinder binder = BRServiceManager.get().getService("locale");
            if (binder == null) return null;
            Class<?> stub = Class.forName("android.app.ILocaleManager$Stub");
            Method asInterface = stub.getMethod("asInterface", IBinder.class);
            return asInterface.invoke(null, binder);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("locale");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return super.invoke(proxy, method, args);
        } catch (Throwable t) {
            Log.w(TAG, "ILocaleManagerProxy exception in " + method.getName() + ": " + t.getMessage());
            Class<?> returnType = method.getReturnType();
            if (LocaleList.class.isAssignableFrom(returnType)) {
                return LocaleList.getEmptyLocaleList();
            }
            if (returnType.equals(boolean.class)) {
                return false;
            }
            return null;
        }
    }

    @ProxyMethod("getApplicationLocales")
    public static class GetApplicationLocales extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (args != null && args.length > 0) {
                    args[0] = BlackBoxCore.getHostPkg();
                }
                return method.invoke(who, args);
            } catch (Throwable t) {
                return LocaleList.getEmptyLocaleList();
            }
        }
    }
}
