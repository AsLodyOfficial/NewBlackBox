package com.mundo.fake.service;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Application;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import com.mundo.env.android.app.BRActivityManagerNative;
import com.mundo.env.android.app.BRActivityManagerOreo;
import com.mundo.env.android.app.BRLoadedApkReceiverDispatcher;
import com.mundo.env.android.app.BRLoadedApkReceiverDispatcherInnerReceiver;
import com.mundo.env.android.app.BRLoadedApkServiceDispatcher;
import com.mundo.env.android.app.BRLoadedApkServiceDispatcherInnerConnection;
import com.mundo.env.android.content.BRContentProviderNative;
import com.mundo.env.android.content.pm.BRUserInfo;
import com.mundo.env.android.util.BRSingleton;
import com.mundo.MundoCore;
import com.mundo.app.BActivityThread;
import com.mundo.core.env.AppSystemEnv;
import com.mundo.core.system.DaemonService;
import com.mundo.core.system.user.BUserHandle;
import com.mundo.entity.AppConfig;
import com.mundo.entity.am.RunningAppProcessInfo;
import com.mundo.entity.am.RunningServiceInfo;
import com.mundo.fake.delegate.ContentProviderDelegate;
import com.mundo.fake.delegate.InnerReceiverDelegate;
import com.mundo.fake.delegate.ServiceConnectionDelegate;
import com.mundo.fake.frameworks.BActivityManager;
import com.mundo.fake.frameworks.BPackageManager;
import com.mundo.fake.hook.ClassInvocationStub;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.fake.hook.ScanClass;
import com.mundo.fake.service.base.PkgMethodProxy;
import com.mundo.fake.service.context.providers.ContentProviderStub;
import com.mundo.fake.service.context.providers.SystemProviderStub;
import com.mundo.proxy.ProxyManifest;
import com.mundo.proxy.record.ProxyBroadcastRecord;
import com.mundo.proxy.record.ProxyPendingRecord;
import com.mundo.utils.ArrayUtils;
import com.mundo.utils.ComponentUtils;
import com.mundo.utils.FileUtils;
import com.mundo.utils.MethodParameterUtils;
import com.mundo.utils.Reflector;
import com.mundo.utils.Slog;
import com.mundo.utils.compat.ActivityManagerCompat;
import com.mundo.utils.compat.BuildCompat;
import com.mundo.utils.compat.ParceledListSliceCompat;
import com.mundo.utils.compat.TaskDescriptionCompat;

import static android.content.Context.RECEIVER_EXPORTED;
import static android.content.Context.RECEIVER_NOT_EXPORTED;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import org.lsposed.lsparanoid.Obfuscate;

/**
 * Created by @officialakshit01 on 3/30/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶 つ０
 * しーＪ
 * 此处无Bug
 */
@Obfuscate
@ScanClass(ActivityManagerCommonProxy.class)
public class IActivityManagerProxy extends ClassInvocationStub {
    public static final String TAG = "ActivityManagerStub";

    @Override
    protected Object getWho() {
        Object iActivityManager = null;
        if (BuildCompat.isOreo()) {
            iActivityManager = BRActivityManagerOreo.get().IActivityManagerSingleton();
        } else if (BuildCompat.isL()) {
            iActivityManager = BRActivityManagerNative.get().gDefault();
        }
        return BRSingleton.get(iActivityManager).get();
    }

    @Override
    protected void inject(Object base, Object proxy) {
        Object iActivityManager = null;
        if (BuildCompat.isOreo()) {
            iActivityManager = BRActivityManagerOreo.get().IActivityManagerSingleton();
        } else if (BuildCompat.isL()) {
            iActivityManager = BRActivityManagerNative.get().gDefault();
        }
        BRSingleton.get(iActivityManager)._set_mInstance(proxy);
    }

    @Override
    public boolean isBadEnv() {
        return getProxyInvocation() != getWho();
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new PkgMethodProxy("getAppStartMode"));
        addMethodHook(new PkgMethodProxy("setAppLockedVerifying"));
        addMethodHook(new PkgMethodProxy("reportJunkFromApp"));
    }

    @ProxyMethod("getContentProvider")
    public static class GetContentProvider extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Exception {
            int authIndex = getAuthIndex();
            Object auth = args[authIndex];
            Object content = null;

            if (auth instanceof String) {
                if (ProxyManifest.isProxy((String) auth)) {
                    return method.invoke(who, args);
                }

                if (BuildCompat.isQ()) {
                    args[1] = MundoCore.getHostPkg();
                }

                if (auth.equals("settings") || auth.equals("media") || auth.equals("telephony")
                        || auth.equals("com.google.android.gsf.gservices")
                        || auth.equals("com.google.android.gsf.gservices.internal")) {
                    content = method.invoke(who, args);
                    if (content != null) {
                        ContentProviderDelegate.update(content, (String) auth);
                    } else {
                        Log.w(TAG, "system getContentProvider returned null for: " + auth);
                    }
                    return content;
                } else {
                    Log.d(TAG, "hook getContentProvider: " + auth);

                    ProviderInfo providerInfo = MundoCore.getBPackageManager().resolveContentProvider((String) auth,
                            FileUtils.FileMode.MODE_IWUSR, BActivityThread.getUserId());
                    if (providerInfo == null) {
                        content = method.invoke(who, args);
                        // Only wrap known system authorities. Wrapping guest-app internal
                        // providers (Finsky's SecurityHubContentProvider, etc.) with a Proxy
                        // causes ClassCastException when Play Services casts it to its concrete type.
                        if (content != null && isKnownSystemAuthority((String) auth)) {
                            ContentProviderDelegate.update(content, (String) auth);
                        }
                        return content;
                    }

                    IBinder providerBinder = null;
                    boolean isLocal = false;
                    if (BActivityThread.getAppPid() != -1) {
                        AppConfig appConfig = MundoCore.getBActivityManager().initProcess(providerInfo.packageName,
                                providerInfo.processName, BActivityThread.getUserId());
                        isLocal = (appConfig.bpid == BActivityThread.getAppPid());
                        if (!isLocal) {
                            providerBinder = MundoCore.getBActivityManager().acquireContentProviderClient(providerInfo);
                        } else {
                            providerBinder = getLocalProviderBinder((String) auth);
                            if (providerBinder == null) {
                                try {
                                    android.content.Context context = null;
                                    if (providerInfo.packageName.equals(BActivityThread.getAppPackageName())
                                            && BActivityThread.getApplication() != null) {
                                        context = BActivityThread.getApplication();
                                    } else {
                                        context = com.mundo.app.BActivityThread
                                                .createPackageContext(providerInfo.applicationInfo);
                                        try {
                                            Object loadedApk = com.mundo.env.android.app.BRContextImpl.get(context)
                                                    .mPackageInfo();
                                            com.mundo.env.android.app.BRLoadedApk.getWithException(loadedApk)
                                                    .makeApplication(false, null);
                                        } catch (Throwable ignored) {
                                        }
                                    }
                                    com.mundo.app.BActivityThread.installProvider(MundoCore.mainThread(), context,
                                            providerInfo, null);
                                    providerBinder = getLocalProviderBinder((String) auth);
                                } catch (Throwable e) {
                                    android.util.Log.e(TAG, "Failed to install provider locally: " + auth, e);
                                }
                            }
                        }
                        args[authIndex] = ProxyManifest.getProxyAuthorities(appConfig.bpid);
                        args[getUserIndex()] = MundoCore.getHostUserId();
                    }
                    if (providerBinder == null)
                        return null;

                    content = method.invoke(who, args);
                    Reflector.with(content).field("info").set(providerInfo);

                    android.os.IInterface realProvider = BRContentProviderNative.get().asInterface(providerBinder);
                    if (isLocal) {
                        Reflector.with(content).field("provider").set(realProvider);
                    } else {
                        // Only wrap known Mundo system providers. Wrapping guest-app internal
                        // providers (Finsky's SecurityHubContentProvider, CachedPackageContentProvider)
                        // with a Java Proxy causes ClassCastException when Play Services casts
                        // the IContentProvider back to its concrete Transport class.
                        if (isKnownSystemAuthority((String) auth)) {
                            Reflector.with(content).field("provider").set(
                                    new ContentProviderStub().wrapper(realProvider, BActivityThread.getAppPackageName()));
                        } else {
                            Reflector.with(content).field("provider").set(realProvider);
                        }
                    }
                }

                return content;
            }
            return method.invoke(who, args);
        }

        private IBinder getLocalProviderBinder(String targetAuth) {
            try {
                Object activityThread = MundoCore.mainThread();
                java.util.Map<?, ?> map = com.mundo.env.android.app.BRActivityThread.get(activityThread).mProviderMap();
                for (Object value : map.values()) {
                    String[] mNames = com.mundo.env.android.app.BRActivityThreadProviderClientRecordP.get(value)
                            .mNames();
                    if (mNames != null) {
                        for (String name : mNames) {
                            if (name.equals(targetAuth)) {
                                android.os.IInterface provider = com.mundo.env.android.app.BRActivityThreadProviderClientRecordP
                                        .get(value).mProvider();
                                if (provider != null)
                                    return provider.asBinder();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        private static boolean isKnownSystemAuthority(String auth) {
            return "settings".equals(auth) || "media".equals(auth) || "telephony".equals(auth)
                    || "com.google.android.gsf.gservices".equals(auth)
                    || "com.google.android.gsf.gservices.internal".equals(auth);
        }

        private int getAuthIndex() {
            // 10.0
            if (BuildCompat.isQ()) {
                return 2;
            } else {
                return 1;
            }
        }

        private int getUserIndex() {
            return getAuthIndex() + 1;
        }
    }

    @ProxyMethod("startService")
    public static class StartService extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Intent intent = (Intent) args[1];
            String resolvedType = (String) args[2];
            if (intent != null) {
                String pkg = intent.getPackage();
                if (pkg == null && intent.getComponent() != null) {
                    pkg = intent.getComponent().getPackageName();
                }
            }
            ResolveInfo resolveInfo = MundoCore.getBPackageManager().resolveService(intent, 0, resolvedType,
                    BActivityThread.getUserId());
            if (resolveInfo == null) {
                return method.invoke(who, args);
            }

            int requireForegroundIndex = getRequireForeground();
            boolean requireForeground = false;
            if (requireForegroundIndex != -1) {
                requireForeground = (boolean) args[requireForegroundIndex];
            }
            return MundoCore.getBActivityManager().startService(intent, resolvedType, requireForeground,
                    BActivityThread.getUserId());
        }

        public int getRequireForeground() {
            if (BuildCompat.isOreo()) {
                return 3;
            }
            return -1;
        }
    }

    @ProxyMethod("stopService")
    public static class StopService extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Intent intent = (Intent) args[1];
            String resolvedType = (String) args[2];
            if (intent != null) {
                String pkg = intent.getPackage();
                if (pkg == null && intent.getComponent() != null) {
                    pkg = intent.getComponent().getPackageName();
                }
            }
            return MundoCore.getBActivityManager().stopService(intent, resolvedType, BActivityThread.getUserId());
        }
    }

    @ProxyMethod("stopServiceToken")
    public static class StopServiceToken extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ComponentName componentName = (ComponentName) args[0];
            IBinder token = (IBinder) args[1];
            MundoCore.getBActivityManager().stopServiceToken(componentName, token, BActivityThread.getUserId());
            return true;
        }
    }

    // TODO 待修复
    @ProxyMethod("bindService")
    public static class BindService extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Intent intent = (Intent) args[2];
            String resolvedType = (String) args[3];
            IServiceConnection connection = (IServiceConnection) args[4];

            if (intent != null) {
                String pkg = intent.getPackage();
                if (pkg == null && intent.getComponent() != null) {
                    pkg = intent.getComponent().getPackageName();
                }
            }

            int userId = intent.getIntExtra("_G_|_UserId", -1);
            userId = userId == -1 ? BActivityThread.getUserId() : userId;
            ResolveInfo resolveInfo = MundoCore.getBPackageManager().resolveService(intent, 0, resolvedType, userId);
            if (resolveInfo != null || AppSystemEnv.isOpenPackage(intent.getComponent())) {
                Intent bindService = MundoCore.getBActivityManager().bindService(intent,
                        connection == null ? null : connection.asBinder(), resolvedType, userId);
                if (connection != null) {
                    if (intent.getComponent() == null && resolveInfo != null) {
                        intent.setComponent(
                                new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name));
                    }
                    IServiceConnection proxy = ServiceConnectionDelegate.createProxy(connection, intent);
                    args[4] = proxy;

                    WeakReference<?> weakReference = BRLoadedApkServiceDispatcherInnerConnection.get(connection)
                            .mDispatcher();
                    if (weakReference != null) {
                        BRLoadedApkServiceDispatcher.get(weakReference.get())._set_mConnection(proxy);
                    }
                }
                if (bindService != null) {
                    args[2] = bindService;
                    return method.invoke(who, args);
                }
                return 0;
            }
            return method.invoke(who, args);
        }

        @Override
        protected boolean isEnable() {
            return MundoCore.get().isBlackProcess() || MundoCore.get().isServerProcess();
        }
    }

    // android 13.0变更
    @ProxyMethod("bindServiceInstance")
    public static class BindServiceInstance extends BindIsolatedService {

    }

    // 10.0
    @ProxyMethod("bindIsolatedService")
    public static class BindIsolatedService extends BindService {
        @Override
        protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
            // instanceName
            args[6] = null;
            return super.beforeHook(who, method, args);
        }
    }

    @ProxyMethod("unbindService")
    public static class UnbindService extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IServiceConnection iServiceConnection = (IServiceConnection) args[0];
            if (iServiceConnection == null) {
                return method.invoke(who, args);
            }
            MundoCore.getBActivityManager().unbindService(iServiceConnection.asBinder(), BActivityThread.getUserId());
            ServiceConnectionDelegate delegate = ServiceConnectionDelegate.getDelegate(iServiceConnection.asBinder());
            if (delegate != null) {
                args[0] = delegate;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getRunningAppProcesses")
    public static class GetRunningAppProcesses extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            RunningAppProcessInfo runningAppProcesses = BActivityManager.get()
                    .getRunningAppProcesses(BActivityThread.getAppPackageName(), BActivityThread.getUserId());
            if (runningAppProcesses == null) {
                return new ArrayList<>();
            }
            return runningAppProcesses.mAppProcessInfoList;
        }
    }

    @ProxyMethod("getServices")
    public static class GetServices extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            RunningServiceInfo runningServices = BActivityManager.get()
                    .getRunningServices(BActivityThread.getAppPackageName(), BActivityThread.getUserId());
            if (runningServices == null) {
                return new ArrayList<>();
            }
            return runningServices.mRunningServiceInfoList;
        }
    }

    @ProxyMethod("getIntentSender")
    public static class GetIntentSender extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int type = (int) args[0];
            Intent[] intents = (Intent[]) args[getIntentsIndex(args)];
            MethodParameterUtils.replaceFirstAppPkg(args);

            for (int i = 0; i < intents.length; i++) {
                Intent intent = intents[i];
                switch (type) {
                    case ActivityManagerCompat.INTENT_SENDER_ACTIVITY:
                        Intent shadow = new Intent();
                        shadow.setComponent(new ComponentName(MundoCore.getHostPkg(),
                                ProxyManifest.getProxyPendingActivity(BActivityThread.getAppPid())));
                        ProxyPendingRecord.saveStub(shadow, intent, BActivityThread.getUserId());
                        intents[i] = shadow;
                        break;
                }
            }

            // Android 12 (API 31) compat: PendingIntent requires FLAG_IMMUTABLE or
            // FLAG_MUTABLE
            if (BuildCompat.isS()) {
                int flagsIndex = getIntentsIndex(args) + 2;
                if (flagsIndex < args.length && args[flagsIndex] instanceof Integer) {
                    int flags = (int) args[flagsIndex];
                    if ((flags & (0x4000000 | 0x2000000)) == 0) {
                        flags |= 0x4000000; // FLAG_IMMUTABLE
                        args[flagsIndex] = flags;
                    }
                }
            }

            IInterface invoke = (IInterface) method.invoke(who, args);
            if (invoke != null) {
                String[] packagesForUid = BPackageManager.get().getPackagesForUid(BActivityThread.getCallingBUid());
                if (packagesForUid.length < 1) {
                    packagesForUid = new String[] { MundoCore.getHostPkg() };
                }
                MundoCore.getBActivityManager().getIntentSender(invoke.asBinder(), packagesForUid[0],
                        BActivityThread.getCallingBUid());
            }
            return invoke;
        }

        private int getIntentsIndex(Object[] args) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent[]) {
                    return i;
                }
            }
            if (BuildCompat.isR()) {
                return 6;
            } else {
                return 5;
            }
        }
    }

    @ProxyMethod("getPackageForIntentSender")
    public static class getPackageForIntentSender extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface invoke = (IInterface) args[0];
            return MundoCore.getBActivityManager().getPackageForIntentSender(invoke.asBinder());
        }
    }

    @ProxyMethod("getUidForIntentSender")
    public static class getUidForIntentSender extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface invoke = (IInterface) args[0];
            return MundoCore.getBActivityManager().getUidForIntentSender(invoke.asBinder());
        }
    }

    @ProxyMethod("getIntentSenderWithSourceToken")
    public static class GetIntentSenderWithSourceToken extends GetIntentSender {
    }

    @ProxyMethod("getIntentSenderWithFeature")
    public static class GetIntentSenderWithFeature extends GetIntentSender {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("broadcastIntent")
    public static class BroadcastIntent extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int intentIndex = getIntentIndex(args);
            Intent intent = (Intent) args[intentIndex];
            String resolvedType = (String) args[intentIndex + 1];

            Intent proxyIntent = MundoCore.getBActivityManager().sendBroadcast(intent, resolvedType,
                    BActivityThread.getUserId());
            if (proxyIntent != null) {
                // getApplication() may be null during GmsApplication.attachBaseContext (before
                // Application.onCreate), so guard against NPE here.
                android.app.Application app = BActivityThread.getApplication();
                if (app != null) {
                    proxyIntent.setExtrasClassLoader(app.getClassLoader());
                }

                ProxyBroadcastRecord.saveStub(proxyIntent, intent, BActivityThread.getUserId());
                args[intentIndex] = proxyIntent;
            }
            // ignore permission and fix userId=-1 (UserHandle.ALL)
            for (int i = 0; i < args.length; i++) {
                Object o = args[i];
                if (o instanceof String[]) {
                    args[i] = null;
                }
            }
            if (args.length > 0 && args[args.length - 1] instanceof Integer) {
                if (((Integer) args[args.length - 1]) == -1) {
                    args[args.length - 1] = 0;
                }
            }
            return method.invoke(who, args);
        }

        int getIntentIndex(Object[] args) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Intent) {
                    return i;
                }
            }
            return 1;
        }
    }

    @ProxyMethod("unregisterReceiver")
    public static class unregisterReceiver extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args[0] instanceof android.content.IIntentReceiver) {
                android.content.IIntentReceiver intentReceiver = (android.content.IIntentReceiver) args[0];
                com.mundo.fake.delegate.InnerReceiverDelegate delegate = com.mundo.fake.delegate.InnerReceiverDelegate
                        .getDelegate(intentReceiver.asBinder());
                if (delegate != null) {
                    args[0] = delegate;
                }
            }
            try {
                return method.invoke(who, args);
            } catch (IllegalArgumentException e) {
                // Frameworks like Oppo might still double unregister or unregister invalid
                // ones.
                // Ignore these to avoid crashing since it's just a system cleanup attempt.
                if (e.getMessage() != null && e.getMessage().contains("Receiver not registered")) {
                    return null;
                }
                throw e;
            }
        }
    }

    @ProxyMethod("finishReceiver")
    public static class finishReceiver extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("publishService")
    public static class PublishService extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("peekService")
    public static class PeekService extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastAppPkg(args);
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            return MundoCore.getBActivityManager().peekService(intent, resolvedType, BActivityThread.getUserId());
        }
    }

    // todo
    @ProxyMethod("sendIntentSender")
    public static class SendIntentSender extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    // android 10
    @ProxyMethod("registerReceiverWithFeature")
    public static class RegisterReceiverWithFeature extends RegisterReceiver {

    }

    @ProxyMethod("registerReceiver")
    public static class RegisterReceiver extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            int receiverIndex = MethodParameterUtils.getIndex(args, IIntentReceiver.class);
            if (receiverIndex != -1 && args[receiverIndex] != null) {
                IIntentReceiver intentReceiver = (IIntentReceiver) args[receiverIndex];
                IIntentReceiver proxy = InnerReceiverDelegate.createProxy(intentReceiver);

                WeakReference<?> weakReference = BRLoadedApkReceiverDispatcherInnerReceiver.get(intentReceiver)
                        .mDispatcher();
                if (weakReference != null) {
                    BRLoadedApkReceiverDispatcher.get(weakReference.get())._set_mIIntentReceiver(proxy);
                }

                args[receiverIndex] = proxy;
            }
            // ignore permission
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String && ((String) args[i]).startsWith("android.permission.")) {
                    args[i] = null;
                }
            }
            return method.invoke(who, args);
        }
    }

    // 这里需要修复
    @ProxyMethod("grantUriPermission")
    public static class GrantUriPermission extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastUid(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setServiceForeground")
    public static class setServiceForeground extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("getHistoricalProcessExitReasons")
    public static class getHistoricalProcessExitReasons extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return ParceledListSliceCompat.create(new ArrayList<>());
        }
    }

    @ProxyMethod("getCurrentUser")
    public static class getCurrentUser extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BRUserInfo.get()._new(BActivityThread.getUserId(), "EliteCore", BRUserInfo.get().FLAG_PRIMARY());
        }
    }

    @ProxyMethod("checkPermission")
    public static class checkPermission extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastUid(args);
            String permission = (String) args[0];
            if (permission != null) {
                switch (permission) {
                    case Manifest.permission.ACCOUNT_MANAGER:
                    case Manifest.permission.SEND_SMS:
                        // GMS: allow config reads and cross-user broadcasts
                    case "android.permission.READ_DEVICE_CONFIG":
                    case "android.permission.WRITE_DEVICE_CONFIG":
                    case "android.permission.INTERACT_ACROSS_USERS":
                    case "android.permission.INTERACT_ACROSS_USERS_FULL":
                    case "android.permission.MANAGE_USERS":
                        // GMS internal permissions (AppDoctorProvider, etc.)
                    case "com.google.android.gms.permission.INTERNAL_BROADCAST":
                    case "com.google.android.gms.permission.INTERNAL_BROADCAST_RECEIVE":
                    case "com.google.android.providers.gsf.permission.READ_GSERVICES":
                    case "com.google.android.c2dm.permission.RECEIVE":
                        return PackageManager.PERMISSION_GRANTED;
                }
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("checkUriPermission")
    public static class checkUriPermission extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    // for < Android 10
    @ProxyMethod("setTaskDescription")
    public static class SetTaskDescription extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ActivityManager.TaskDescription td = (ActivityManager.TaskDescription) args[1];
            args[1] = TaskDescriptionCompat.fix(td);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setRequestedOrientation")
    public static class setRequestedOrientation extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    @ProxyMethod("registerUidObserver")
    public static class registerUidObserver extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("unregisterUidObserver")
    public static class unregisterUidObserver extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("updateConfiguration")
    public static class updateConfiguration extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /**
     * GMS persistent process (e.g. TrustedTime) calls sendBroadcastAsUser() with
     * UserHandle.ALL (-1) which triggers:
     * SecurityException: Permission Denial: broadcast from com.bgmi asks to run as
     * user -1 ... requires INTERACT_ACROSS_USERS_FULL
     *
     * Fix: remap userId=-1 to the host userId (0), and swallow SecurityExceptions
     * that slip through so GMS does not crash.
     */
    @ProxyMethod("broadcastIntentWithFeature")
    public static class BroadcastIntentWithFeature extends BroadcastIntent {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            // Clamp last integer arg (userId) from -1 to 0
            for (int i = args.length - 1; i >= 0; i--) {
                if (args[i] instanceof Integer) {
                    if ((int) args[i] == -1) {
                        args[i] = 0;
                    }
                    break;
                }
            }
            try {
                return super.hook(who, method, args);
            } catch (SecurityException se) {
                // Absorb INTERACT_ACROSS_USERS denials from GMS service broadcasts
                Log.d(TAG, "broadcastIntentWithFeature suppressed SecurityException: " + se.getMessage());
                return 0;
            }
        }
    }

}
