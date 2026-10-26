package top.niunaijun.blackbox.fake.service;

import android.content.pm.PackageManager;

import black.android.app.BRActivityThread;
import black.android.app.BRContextImpl;
import black.android.os.BRServiceManager;
import black.android.permission.BRIPermissionManagerStub;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.service.base.PkgMethodProxy;
import top.niunaijun.blackbox.fake.service.base.ValueMethodProxy;
import top.niunaijun.blackbox.utils.Reflector;
import top.niunaijun.blackbox.utils.compat.BuildCompat;


public class IPermissionManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IPermissionManagerProxy";

    private static final String P = "permissionmgr";

    public IPermissionManagerProxy() {
        super(BRServiceManager.get().getService(P));
    }

    @Override
    protected Object getWho() {
        return BRIPermissionManagerStub.get().asInterface(BRServiceManager.get().getService(P));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("permissionmgr");
        BRActivityThread.getWithException()._set_sPermissionManager(proxyInvocation);
        
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("addPermissionAsync", true));
        addMethodHook(new ValueMethodProxy("addPermission", true));
        addMethodHook(new ValueMethodProxy("performDexOpt", true));
        addMethodHook(new ValueMethodProxy("performDexOptIfNeeded", false));
        addMethodHook(new ValueMethodProxy("performDexOptSecondary", true));
        addMethodHook(new ValueMethodProxy("addOnPermissionsChangeListener", 0));
        addMethodHook(new ValueMethodProxy("removeOnPermissionsChangeListener", 0));
        addMethodHook(new ValueMethodProxy("checkDeviceIdentifierAccess", false));
        addMethodHook(new PkgMethodProxy("shouldShowRequestPermissionRationale"));
        if (BuildCompat.isOreo()) {
            addMethodHook(new ValueMethodProxy("notifyDexLoad", 0));
            addMethodHook(new ValueMethodProxy("notifyPackageUse", 0));
            addMethodHook(new ValueMethodProxy("setInstantAppCookie", false));
            addMethodHook(new ValueMethodProxy("isInstantApp", false));
        }
        addMethodHook(new CheckPermissionHook());
        addMethodHook(new CheckUidPermissionHook());
        addMethodHook(new GetPackagesForUidHook());
    }

    public static class GetPackagesForUidHook extends top.niunaijun.blackbox.fake.hook.MethodHook {
        @Override
        protected String getMethodName() {
            return "getPackagesForUid";
        }

        @Override
        protected Object hook(Object who, java.lang.reflect.Method method, Object[] args) throws Throwable {
            int uid = (Integer) args[0];
            if (uid == android.os.Process.SYSTEM_UID || uid == 0 || uid == 1000) {
                return new String[]{"android"};
            }
            if (uid == BlackBoxCore.getHostUid()) {
                uid = top.niunaijun.blackbox.app.BActivityThread.getBUid();
            }
            String[] packagesForUid = BlackBoxCore.getBPackageManager().getPackagesForUid(uid);
            android.util.Log.d(TAG, "IPermissionManagerProxy getPackagesForUid(" + uid + ") -> " + java.util.Arrays.toString(packagesForUid));
            if (packagesForUid != null && packagesForUid.length > 0) {
                return packagesForUid;
            }
            if (uid == android.os.Process.SYSTEM_UID || uid == 1000) {
                return new String[]{"android"};
            }
            try {
                return method.invoke(who, args);
            } catch (Throwable t) {
                return new String[]{top.niunaijun.blackbox.app.BActivityThread.getAppPackageName()};
            }
        }
    }

    public static class CheckPermissionHook extends top.niunaijun.blackbox.fake.hook.MethodHook {
        @Override
        protected String getMethodName() {
            return "checkPermission";
        }

        @Override
        protected Object hook(Object who, java.lang.reflect.Method method, Object[] args) throws Throwable {
            android.util.Log.d(TAG, "IPermissionManagerProxy checkPermission called! args: " + java.util.Arrays.toString(args));
            if (args != null) {
                String permission = null;
                for (Object arg : args) {
                    if (arg instanceof String && ((String)arg).startsWith("android.permission.")) {
                        permission = (String) arg;
                    }
                }
                if (permission == null && args.length > 1 && args[1] instanceof String) {
                    permission = (String) args[1];
                }
                if ("android.permission.ACCOUNT_MANAGER".equals(permission)) {
                    android.util.Log.d(TAG, "IPermissionManagerProxy checkPermission: Granting ACCOUNT_MANAGER!");
                    return PackageManager.PERMISSION_GRANTED;
                }
            }
            return PackageManager.PERMISSION_GRANTED; // Just grant EVERYTHING for now to test!!!
        }
    }

    public static class CheckUidPermissionHook extends top.niunaijun.blackbox.fake.hook.MethodHook {
        @Override
        protected String getMethodName() {
            return "checkUidPermission";
        }

        @Override
        protected Object hook(Object who, java.lang.reflect.Method method, Object[] args) throws Throwable {
            android.util.Log.d(TAG, "IPermissionManagerProxy checkUidPermission called! args: " + java.util.Arrays.toString(args));
            if (args != null) {
                String permission = null;
                for (Object arg : args) {
                    if (arg instanceof String && ((String)arg).startsWith("android.permission.")) {
                        permission = (String) arg;
                        break;
                    }
                }
                if (permission == null && args.length > 1 && args[1] instanceof String) {
                    permission = (String) args[1];
                }
                if ("android.permission.ACCOUNT_MANAGER".equals(permission)) {
                    android.util.Log.d(TAG, "IPermissionManagerProxy checkUidPermission: Granting ACCOUNT_MANAGER!");
                    return PackageManager.PERMISSION_GRANTED;
                }
            }
            return PackageManager.PERMISSION_GRANTED; // Just grant EVERYTHING for now to test!!!
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

}




