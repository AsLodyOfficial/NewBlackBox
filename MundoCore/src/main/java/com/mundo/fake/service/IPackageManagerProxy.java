package com.mundo.fake.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mundo.env.android.app.BRActivityThread;
import com.mundo.env.android.app.BRContextImpl;

import com.mundo.MundoCore;
import com.mundo.app.BActivityThread;
import com.mundo.core.GmsCore;
import com.mundo.core.env.AppSystemEnv;
import com.mundo.fake.hook.BinderInvocationStub;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.fake.service.base.PkgMethodProxy;
import com.mundo.fake.service.base.ValueMethodProxy;
import com.mundo.utils.MethodParameterUtils;
import com.mundo.utils.Reflector;
import com.mundo.utils.compat.BuildCompat;
import com.mundo.utils.compat.ParceledListSliceCompat;

/**
 * IPackageManagerProxy — Production Final
 *
 * CRITICAL DESIGN DECISIONS for Facebook OAuth:
 *
 * 1. queryIntentActivities for http/https is NOT blocked.
 * REASON: Facebook SDK's CustomTabMainActivity.onCreate() calls
 * queryIntentActivities(ACTION_VIEW, https) to check if ANY browser
 * is available before trying to launch the CCT. If we return empty,
 * the SDK calls finish() immediately (21ms after creation) without
 * ever calling startActivity(https_url). That https_url call is what
 * ActivityManagerCommonProxy.StartActivity intercepts to redirect to
 * ProxyWebActivity. Blocking it kills the entire flow.
 *
 * 2. resolveService for CCT IS blocked.
 * This prevents Chrome from binding as a CCT service provider.
 * The SDK falls back to a regular View intent for the URL,
 * which we then intercept in StartActivity.hook().
 *
 * 3. queryIntentServices is NOT blocked.
 * SDK's isCustomTabsSupported() calls this. Blocking → RESULT_CANCELED.
 */
public class IPackageManagerProxy extends BinderInvocationStub {

    public static final String TAG = "PackageManagerStub";

    public static boolean isFacebookInstalledInCore() {
        return MundoCore.get().isInstalled("com.facebook.katana", BActivityThread.getUserId()) ||
               MundoCore.get().isInstalled("com.facebook.lite", BActivityThread.getUserId());
    }

    public static boolean isWhatsAppInstalledInCore() {
        return MundoCore.get().isInstalled("com.whatsapp", BActivityThread.getUserId());
    }


    private static final String CCT_ACTION_SUPPORT = "android.support.customtabs.action.CustomTabsService";
    private static final String CCT_ACTION_ANDROIDX = "androidx.browser.customtabs.CustomTabsService";

    private static boolean isCustomTabsServiceIntent(Intent intent) {
        if (intent == null)
            return false;
        String a = intent.getAction();
        return CCT_ACTION_SUPPORT.equals(a) || CCT_ACTION_ANDROIDX.equals(a);
    }

    private static Object wrapResolveList(List<ResolveInfo> list) {
        if (BuildCompat.isN())
            return ParceledListSliceCompat.create(list);
        return list;
    }

    private static List<ResolveInfo> filterResolveInfo(List<ResolveInfo> list) {
        if (list == null)
            return null;
        List<ResolveInfo> filtered = new ArrayList<>();
        for (ResolveInfo ri : list) {
            if (ri != null && ri.activityInfo != null) {
                String pkg = ri.activityInfo.packageName;
                if (pkg != null && (pkg.startsWith("com.facebook.katana")
                        || pkg.startsWith("com.facebook.orca")
                        || pkg.startsWith("com.facebook.lite")
                        || pkg.startsWith("com.facebook.mlite"))) {
                    continue;
                }
            }
            filtered.add(ri);
        }
        return filtered;
    }

    public IPackageManagerProxy() {
        super(BRActivityThread.get().sPackageManager().asBinder());
    }

    @Override
    protected Object getWho() {
        return BRActivityThread.get().sPackageManager();
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        try {
            BRActivityThread.get()._set_sPackageManager(proxyInvocation);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            replaceSystemService("package");
        } catch (Throwable t) {
            t.printStackTrace();
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
                    BRContextImpl.get(systemContext).getPackageManager();
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

    private void injectPackageManager(PackageManager pm, Object proxyInvocation) {
        if (pm == null)
            return;
        try {
            Reflector.on("android.app.ApplicationPackageManager")
                    .field("mPM").set(pm, proxyInvocation);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("addOnPermissionsChangeListener", 0));
        addMethodHook(new ValueMethodProxy("removeOnPermissionsChangeListener", 0));
        addMethodHook(new PkgMethodProxy("shouldShowRequestPermissionRationale"));
    }

    @ProxyMethod("resolveIntent")
    public static class ResolveIntent extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Intent intent = (Intent) args[0];
            String type = (String) args[1];
            int flags = Integer.parseInt(args[2] + "");
            ResolveInfo ri = MundoCore.getBPackageManager()
                    .resolveIntent(intent, type, flags, BActivityThread.getUserId());
            if (ri != null)
                return ri;
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("resolveService")
    public static class ResolveService extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Intent intent = (Intent) args[0];
            String type = (String) args[1];
            int flags = Integer.parseInt(args[2] + "");
            // Block CCT service binding — prevents Chrome from connecting as CCT provider.
            // SDK falls back to regular startActivity(https_url) which we intercept.
            if (isCustomTabsServiceIntent(intent))
                return null;
            ResolveInfo ri = MundoCore.getBPackageManager()
                    .resolveService(intent, flags, type, BActivityThread.getUserId());
            if (ri != null)
                return ri;
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("queryIntentActivities")
    public static class QueryIntentActivities extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Intent intent = args.length > 0 && args[0] instanceof Intent ? (Intent) args[0] : null;
            String type = args.length > 1 && args[1] instanceof String ? (String) args[1] : null;
            int flags = args.length > 2 ? MethodParameterUtils.toInt(args[2]) : 0;
            List<ResolveInfo> vr = MundoCore.getBPackageManager()
                    .queryIntentActivities(intent, flags, type, BActivityThread.getUserId());
            if (vr != null && !vr.isEmpty())
                return wrapResolveList(filterResolveInfo(vr));
            Object hostRes = method.invoke(who, args);
            if (hostRes instanceof List) {
                return filterResolveInfo((List<ResolveInfo>) hostRes);
            } else if (hostRes != null && hostRes.getClass().getName().contains("ParceledListSlice")) {
                try {
                    List<ResolveInfo> list = Reflector.with(hostRes).method("getList").call();
                    return wrapResolveList(filterResolveInfo(list));
                } catch (Exception ignored) {
                }
            }
            return hostRes;
        }
    }

    // queryIntentServices: NOT blocked — SDK uses it for isCustomTabsSupported()

    @ProxyMethod("setComponentEnabledSetting")
    public static class SetComponentEnabledSetting extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("getPackageInfo")
    public static class GetPackageInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String pkg = (String) args[0];
            int flags = MethodParameterUtils.toInt(args[1]);
            PackageInfo pi = MundoCore.getBPackageManager().getPackageInfo(pkg, flags, BActivityThread.getUserId());
            if (pi != null)
                return pi;
            if (AppSystemEnv.isOpenPackage(pkg))
                return method.invoke(who, args);
            return null;
        }
    }

    @ProxyMethod("getPackageUid")
    public static class GetPackageUid extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String pkg = (args != null && args.length > 0 && args[0] instanceof String) ? (String) args[0] : null;
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getPackageUidEtc")
    public static class getPackageUidEtc extends GetPackageUid {
    }

    @ProxyMethod("setApplicationBlockedSettingAsUser")
    public static class setApplicationBlockedSettingAsUser extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            MethodParameterUtils.replaceLastUserId(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getProviderInfo")
    public static class GetProviderInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ComponentName cn = (ComponentName) args[0];
            int flags = MethodParameterUtils.toInt(args[1]);
            ProviderInfo pi = MundoCore.getBPackageManager().getProviderInfo(cn, flags, BActivityThread.getUserId());
            if (pi != null)
                return pi;
            if (AppSystemEnv.isOpenPackage(cn))
                return method.invoke(who, args);
            return null;
        }
    }

    @ProxyMethod("getReceiverInfo")
    public static class GetReceiverInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ComponentName cn = (ComponentName) args[0];
            int flags = MethodParameterUtils.toInt(args[1]);
            ActivityInfo ai = MundoCore.getBPackageManager().getReceiverInfo(cn, flags, BActivityThread.getUserId());
            if (ai != null)
                return ai;
            if (AppSystemEnv.isOpenPackage(cn))
                return method.invoke(who, args);
            return null;
        }
    }

    @ProxyMethod("getActivityInfo")
    public static class GetActivityInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ComponentName cn = (ComponentName) args[0];
            int flags = MethodParameterUtils.toInt(args[1]);
            ActivityInfo ai = MundoCore.getBPackageManager().getActivityInfo(cn, flags, BActivityThread.getUserId());
            if (ai != null)
                return ai;
            if (AppSystemEnv.isOpenPackage(cn))
                return method.invoke(who, args);
            return null;
        }
    }

    @ProxyMethod("getServiceInfo")
    public static class GetServiceInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ComponentName cn = (ComponentName) args[0];
            int flags = MethodParameterUtils.toInt(args[1]);
            ServiceInfo si = MundoCore.getBPackageManager().getServiceInfo(cn, flags, BActivityThread.getUserId());
            if (si != null)
                return si;
            if (AppSystemEnv.isOpenPackage(cn))
                return method.invoke(who, args);
            return null;
        }
    }

    @ProxyMethod("getInstalledApplications")
    public static class GetInstalledApplications extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return ParceledListSliceCompat.create(MundoCore.getBPackageManager()
                    .getInstalledApplications(MethodParameterUtils.toInt(args[0]), BActivityThread.getUserId()));
        }
    }

    @ProxyMethod("getInstalledPackages")
    public static class GetInstalledPackages extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return ParceledListSliceCompat.create(MundoCore.getBPackageManager()
                    .getInstalledPackages(MethodParameterUtils.toInt(args[0]), BActivityThread.getUserId()));
        }
    }

    @ProxyMethod("getApplicationInfo")
    public static class GetApplicationInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String pkg = (String) args[0];
            int flags = MethodParameterUtils.toInt(args[1]);
            ApplicationInfo ai = MundoCore.getBPackageManager().getApplicationInfo(pkg, flags,
                    BActivityThread.getUserId());
            if (ai != null)
                return ai;
            if (AppSystemEnv.isOpenPackage(pkg))
                return method.invoke(who, args);
            return null;
        }
    }

    @ProxyMethod("queryContentProviders")
    public static class QueryContentProviders extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return ParceledListSliceCompat.create(MundoCore.getBPackageManager()
                    .queryContentProviders(BActivityThread.getAppProcessName(),
                            BActivityThread.getBUid(), MethodParameterUtils.toInt(args[2]),
                            BActivityThread.getUserId()));
        }
    }

    @ProxyMethod("queryIntentReceivers")
    public static class QueryBroadcastReceivers extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Intent intent = MethodParameterUtils.getFirstParam(args, Intent.class);
            String type = MethodParameterUtils.getFirstParam(args, String.class);
            Integer flags = MethodParameterUtils.getFirstParam(args, Integer.class);
            List<ResolveInfo> res = MundoCore.getBPackageManager()
                    .queryBroadcastReceivers(intent, flags != null ? flags : 0, type, BActivityThread.getUserId());
            return BuildCompat.isN() ? ParceledListSliceCompat.create(res) : res;
        }
    }

    @ProxyMethod("resolveContentProvider")
    public static class ResolveContentProvider extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String authority = (String) args[0];
            int flags = MethodParameterUtils.toInt(args[1]);
            if (authority != null && (authority.contains("com.google") || authority.contains("firebase")
                    || authority.contains("gservices"))) {
                return method.invoke(who, args);
            }
            ProviderInfo pi = MundoCore.getBPackageManager()
                    .resolveContentProvider(authority, flags, BActivityThread.getUserId());
            return pi != null ? pi : method.invoke(who, args);
        }
    }

    @ProxyMethod("canRequestPackageInstalls")
    public static class CanRequestPackageInstalls extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getPackagesForUid")
    public static class GetPackagesForUid extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int uid = MethodParameterUtils.toInt(args[0]);
            if (uid == MundoCore.getHostUid())
                args[0] = BActivityThread.getBUid();
            return MundoCore.getBPackageManager().getPackagesForUid(MethodParameterUtils.toInt(args[0]));
        }
    }

    @ProxyMethod("getNameForUid")
    public static class GetNameForUid extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int uid = MethodParameterUtils.toInt(args[0]);
            if (uid == MundoCore.getHostUid())
                uid = BActivityThread.getBUid();
            String[] pkgs = MundoCore.getBPackageManager().getPackagesForUid(uid);
            if (pkgs != null && pkgs.length > 0) {
                return pkgs[0];
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getNamesForUids")
    public static class GetNamesForUids extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int[] uids = (int[]) args[0];
            String[] names = new String[uids.length];
            for (int i = 0; i < uids.length; i++) {
                int uid = uids[i];
                if (uid == MundoCore.getHostUid())
                    uid = BActivityThread.getBUid();
                String[] pkgs = MundoCore.getBPackageManager().getPackagesForUid(uid);
                if (pkgs != null && pkgs.length > 0) {
                    names[i] = pkgs[0];
                } else {
                    names[i] = null;
                }
            }
            return names;
        }
    }

    @ProxyMethod("getInstallerPackageName")
    public static class GetInstallerPackageName extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return GmsCore.VENDING_PKG;
        }
    }

    @ProxyMethod("getSharedLibraries")
    public static class GetSharedLibraries extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return ParceledListSliceCompat.create(new ArrayList<>());
        }
    }

    @ProxyMethod("getComponentEnabledSetting")
    public static class getComponentEnabledSetting extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ComponentName cn = (ComponentName) args[0];
            ApplicationInfo ai = MundoCore.getBPackageManager()
                    .getApplicationInfo(cn.getPackageName(), 0, BActivityThread.getUserId());
            if (ai != null)
                return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
            if (AppSystemEnv.isOpenPackage(cn))
                return method.invoke(who, args);
            throw new IllegalArgumentException();
        }
    }

    @ProxyMethod("addPackageToPreferred")
    public static class addPackageToPreferred extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("setSplashScreenTheme")
    public static class SetSplashScreenTheme extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            boolean xiaomi = BuildCompat.isMIUI()
                    || Build.MANUFACTURER.toLowerCase().contains("xiaomi")
                    || Build.BRAND.toLowerCase().contains("xiaomi");
            if (xiaomi)
                return null;
            try {
                return method.invoke(who, args);
            } catch (SecurityException e) {
                return null;
            } catch (Exception e) {
                if (e.getCause() instanceof SecurityException)
                    return null;
                throw e;
            }
        }
    }

    @ProxyMethod("checkSignatures")
    public static class checkSignatures extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args.length == 2 && args[0] instanceof String && args[1] instanceof String) {
                String p1 = (String) args[0];
                String p2 = (String) args[1];
                if (p1 != null && p2 != null) {
                }
                try {
                    PackageManager pm = MundoCore.getPackageManager();
                    PackageInfo p1Info = null;
                    if (MundoCore.getBPackageManager().isInstalled(p1, BActivityThread.getUserId())) {
                        p1Info = MundoCore.getBPackageManager().getPackageInfo(p1, PackageManager.GET_SIGNATURES,
                                BActivityThread.getUserId());
                    } else {
                        p1Info = pm.getPackageInfo(p1, PackageManager.GET_SIGNATURES);
                    }
                    PackageInfo p2Info = null;
                    if (MundoCore.getBPackageManager().isInstalled(p2, BActivityThread.getUserId())) {
                        p2Info = MundoCore.getBPackageManager().getPackageInfo(p2, PackageManager.GET_SIGNATURES,
                                BActivityThread.getUserId());
                    } else {
                        p2Info = pm.getPackageInfo(p2, PackageManager.GET_SIGNATURES);
                    }
                    Signature[] s1 = p1Info != null ? p1Info.signatures : null;
                    Signature[] s2 = p2Info != null ? p2Info.signatures : null;
                    if (s1 == null || s1.length == 0)
                        return (s2 == null || s2.length == 0) ? 1 : -1;
                    if (s2 == null || s2.length == 0)
                        return -2;
                    return Arrays.equals(s1, s2) ? 0 : -3;
                } catch (Exception ignored) {
                }
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("checkUidSignatures")
    public static class checkUidSignatures extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("clearPackagePersistentPreferredActivities")
    public static class clearPackagePersistentPreferredActivities extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("clearPackagePreferredActivities")
    public static class clearPackagePreferredActivities extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getApplicationBlockedSettingAsUser")
    public static class getApplicationBlockedSettingAsUser extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setPackageStoppedState")
    public static class setPackageStoppedState extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
