package top.niunaijun.blackbox.fake.service;

import android.content.pm.PackageManager;
import android.util.Log;

import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;

public class IPermissionCheckerProxy extends BinderInvocationStub {
    public static final String TAG = "IPermissionCheckerProxy";

    public IPermissionCheckerProxy() {
        super(BRServiceManager.get().getService("permission_checker"));
    }

    @Override
    protected Object getWho() {
        // Need to return IPermissionChecker.Stub.asInterface(...)
        // Since we don't have the black reflection class for it, we can just use reflection.
        try {
            Class<?> stubClass = Class.forName("android.permission.IPermissionChecker$Stub");
            java.lang.reflect.Method asInterface = stubClass.getMethod("asInterface", android.os.IBinder.class);
            return asInterface.invoke(null, BRServiceManager.get().getService("permission_checker"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("permission_checker");
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new CheckPermissionHook());
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    public static class CheckPermissionHook extends MethodHook {
        @Override
        protected String getMethodName() {
            return "checkPermission";
        }

        @Override
        protected Object hook(Object who, java.lang.reflect.Method method, Object[] args) throws Throwable {
            Log.d(TAG, "checkPermission called! args: " + java.util.Arrays.toString(args));
            if (args != null) {
                String permission = null;
                for (Object arg : args) {
                    if (arg instanceof String && ((String) arg).startsWith("android.permission.")) {
                        permission = (String) arg;
                    }
                }
                if (permission != null && (permission.equals("android.permission.ACCOUNT_MANAGER")
                        || permission.equals("android.permission.INTERNET")
                        || permission.equals("android.permission.ACCESS_NETWORK_STATE")
                        || permission.equals("android.permission.ACCESS_WIFI_STATE")
                        || permission.equals("android.permission.CHANGE_NETWORK_STATE")
                        || permission.equals("android.permission.CHANGE_WIFI_STATE"))) {
                    Log.d(TAG, "Granting " + permission + " in IPermissionChecker!");
                    return PackageManager.PERMISSION_GRANTED; // PermissionChecker.PERMISSION_GRANTED is 0
                }
            }
            return method.invoke(who, args);
        }
    }
}
