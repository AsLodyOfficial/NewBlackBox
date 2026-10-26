package com.mundo.fake.service;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import java.lang.reflect.Method;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.utils.MethodParameterUtils;

import com.mundo.env.android.app.BRActivityThread;
import com.mundo.env.android.app.BRContextImpl;
import com.mundo.env.android.os.BRServiceManager;
import com.mundo.env.android.permission.BRIPermissionManagerStub;

import com.mundo.MundoCore;
import com.mundo.fake.hook.BinderInvocationStub;
import com.mundo.fake.service.base.PkgMethodProxy;
import com.mundo.fake.service.base.ValueMethodProxy;
import com.mundo.utils.Reflector;
import com.mundo.utils.compat.BuildCompat;

/**
 * Fixed for Android 10–15
 * No mid-game crash
 */
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
        try {
            replaceSystemService("permissionmgr");
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            BRActivityThread.getWithException()._set_sPermissionManager(proxyInvocation);
        } catch (Throwable t) {
            try {
                BRActivityThread.get()._set_sPermissionManager(proxyInvocation);
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        }

        try {
            injectPackageManager(MundoCore.getContext().getPackageManager(), proxyInvocation);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            Object mainThread = MundoCore.mainThread();
            if (mainThread != null) {
                Object systemContext = BRActivityThread.get(mainThread).getSystemContext();
                if (systemContext != null) {
                    PackageManager pm = BRContextImpl.get(systemContext).mPackageManager();
                    if (pm != null) {
                        injectPackageManager(pm, proxyInvocation);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void injectPackageManager(PackageManager packageManager, Object proxyInvocation) {
        if (packageManager == null) return;
        try {
            Object mPermissionManager = Reflector.on("android.app.ApplicationPackageManager")
                    .field("mPermissionManager")
                    .get(packageManager);
            if (mPermissionManager != null) {
                if (mPermissionManager.getClass().getName().equals("android.permission.PermissionManager")) {
                    try {
                        Reflector.on("android.permission.PermissionManager")
                                .field("mPermissionManager")
                                .set(mPermissionManager, proxyInvocation);
                    } catch (Throwable e1) {
                        try {
                            Reflector.on("android.permission.PermissionManager")
                                    .field("mService")
                                    .set(mPermissionManager, proxyInvocation);
                        } catch (Throwable e2) {
                            e2.printStackTrace();
                        }
                    }
                } else {
                    Reflector.on("android.app.ApplicationPackageManager")
                            .field("mPermissionManager")
                            .set(packageManager, proxyInvocation);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
    }

    /**
     * On Android 12+, DeviceConfig.enforceReadPermission() uses
     * PermissionChecker.checkPermissionForPreflight() which calls
     * IPermissionManager.checkPermission() rather than
     * IActivityManager.checkPermission().
     * Granting READ_DEVICE_CONFIG here prevents the SecurityException
     * that crashes GservicesProvider.onCreate() on these versions.
     */
    @ProxyMethod("checkPermission")
    public static class CheckPermissionProxy extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                String permission = (String) args[0];
                switch (permission) {
                    case "android.permission.READ_DEVICE_CONFIG":
                    case "android.permission.WRITE_DEVICE_CONFIG":
                    case "android.permission.INTERACT_ACROSS_USERS":
                    case "android.permission.INTERACT_ACROSS_USERS_FULL":
                    case "android.permission.MANAGE_USERS":
                    case Manifest.permission.ACCOUNT_MANAGER:
                    // GMS internal permissions (AppDoctorProvider, C2DM, GServices, etc.)
                    case "com.google.android.gms.permission.INTERNAL_BROADCAST":
                    case "com.google.android.gms.permission.INTERNAL_BROADCAST_RECEIVE":
                    case "com.google.android.providers.gsf.permission.READ_GSERVICES":
                    case "com.google.android.c2dm.permission.RECEIVE":
                        return PackageManager.PERMISSION_GRANTED;
                }
            }
            MethodParameterUtils.replaceLastUid(args);
            return method.invoke(who, args);
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

}
