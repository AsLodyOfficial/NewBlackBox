package com.mundo.fake.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import java.io.File;
import java.lang.reflect.Method;

import com.mundo.MundoCore;
import com.mundo.app.BActivityThread;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.fake.provider.FileProviderHandler;
import com.mundo.utils.ComponentUtils;
import com.mundo.utils.FileUtils;
import com.mundo.utils.MethodParameterUtils;
import com.mundo.utils.Slog;
import com.mundo.utils.compat.BuildCompat;
import com.mundo.utils.compat.StartActivityCompat;
import org.lsposed.lsparanoid.Obfuscate;

/**
 * ActivityManagerCommonProxy
 *
 * Intercepts AMS startActivity Binder calls from the virtual process.
 *
 * Facebook login flow:
 *  1. Game SDK starts FacebookActivity (virtual, inside game process)
 *  2. FB SDK tries com.facebook.katana/.ProxyAuth for native SSO
 *  3. isBlockedFacebookNativeSso() returns 0 — SDK falls back to its own WebView
 *  4. FacebookActivity WebView shows Facebook login page
 *  5. Login completes, result returned to game SDK normally
 *
 * Browser intents (http/https) that cannot be resolved virtually
 * fall through to the host OS directly.
 */
@Obfuscate
public class ActivityManagerCommonProxy {

    public static final String TAG = "ActivityManagerCommonProxy";

    /**
     * Returns true when the FB SDK is attempting native app SSO via ProxyAuth.
     * Blocking it (return 0) forces the SDK into its own WebView login fallback.
     * Works on Android 5–17: no ProxyAuth activity ever starts, so the SDK
     * self-corrects without any error shown to the user.
     */
    private static boolean isBlockedFacebookNativeSso(Intent intent) {
        if (intent == null) return false;
        ComponentName cn = intent.getComponent();
        if (cn == null) return false;
        String pkg = cn.getPackageName();
        String cls = cn.getClassName();
        boolean isFbFamily = pkg.equals("com.facebook.katana")
                || pkg.equals("com.facebook.wakizashi")
                || pkg.equals("com.facebook.orca")
                || pkg.equals("com.facebook.lite")
                || pkg.equals("com.instagram.android");
        return isFbFamily && cls.contains("ProxyAuth");
    }

    @ProxyMethod("startActivity")
    public static class StartActivity extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            Intent intent = getIntent(args);
            Slog.d(TAG, "Hook startActivity: " + intent);
            if (intent == null) return method.invoke(who, args);

            // Guard: never re-intercept already-wrapped virtual intents
            boolean hasTarget;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasTarget = intent.getParcelableExtra("_G_|_target_", android.os.Parcelable.class) != null;
            } else {
                //noinspection deprecation
                hasTarget = intent.getParcelableExtra("_G_|_target_") != null;
            }
            if (hasTarget) return method.invoke(who, args);

            // Block Facebook/Instagram native SSO — force SDK web-login fallback.
            // We return START_CLASS_NOT_FOUND (-2) so the FB SDK immediately knows
            // the native app cannot handle the login and fires its internal
            // FacebookActivity WebView instead of waiting forever for onActivityResult.
            if (isBlockedFacebookNativeSso(intent)) {
                Slog.d(TAG, "[FB] Blocking native SSO → forcing WebView login: " + intent.getComponent());
                return -2; // ActivityManager.START_CLASS_NOT_FOUND
            }

            // APK install request
            if (ComponentUtils.isRequestInstall(intent)) {
                File file = FileProviderHandler.convertFile(
                        BActivityThread.getApplication(), intent.getData());
                if (MundoCore.get().requestInstallPackage(file)) return 0;
                intent.setData(FileProviderHandler.convertFileUri(
                        BActivityThread.getApplication(), intent.getData()));
                return method.invoke(who, args);
            }

            // Self-package settings redirect
            String dataString = intent.getDataString();
            if (dataString != null
                    && dataString.equals("package:" + BActivityThread.getAppPackageName())) {
                intent.setData(Uri.parse("package:" + MundoCore.getHostPkg()));
            }

            // Try to resolve inside the virtual PM
            ResolveInfo resolveInfo = MundoCore.getBPackageManager().resolveActivity(
                    intent, FileUtils.FileMode.MODE_IWUSR,
                    StartActivityCompat.getResolvedType(args), BActivityThread.getUserId());

            if (resolveInfo == null) {
                String origPkg = intent.getPackage();
                if (intent.getPackage() == null && intent.getComponent() == null)
                    intent.setPackage(BActivityThread.getAppPackageName());
                else
                    origPkg = intent.getPackage();

                resolveInfo = MundoCore.getBPackageManager().resolveActivity(
                        intent, FileUtils.FileMode.MODE_IWUSR,
                        StartActivityCompat.getResolvedType(args), BActivityThread.getUserId());

                if (resolveInfo == null) {
                    // Cannot resolve in virtual env — fall through to host OS
                    intent.setPackage(origPkg);
                    return method.invoke(who, args);
                }
            }

            intent.setExtrasClassLoader(who.getClass().getClassLoader());
            intent.setComponent(new ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name));
            MundoCore.getBActivityManager().startActivityAms(
                    BActivityThread.getUserId(),
                    StartActivityCompat.getIntent(args),
                    StartActivityCompat.getResolvedType(args),
                    StartActivityCompat.getResultTo(args),
                    StartActivityCompat.getResultWho(args),
                    StartActivityCompat.getRequestCode(args),
                    StartActivityCompat.getFlags(args),
                    StartActivityCompat.getOptions(args));
            return 0;
        }

        private Intent getIntent(Object[] args) {
            // Android 10+ (R/API29): index 3; below: index 2
            // Android 13+ may shift further — scan all args as fallback
            int index = BuildCompat.isR() ? 3 : 2;
            if (index < args.length && args[index] instanceof Intent) return (Intent) args[index];
            for (Object arg : args) if (arg instanceof Intent) return (Intent) arg;
            return null;
        }
    }

    @ProxyMethod("startActivities")
    public static class StartActivities extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int index = BuildCompat.isR() ? 3 : 2;
            Intent[]  intents       = (Intent[])  args[index++];
            String[]  resolvedTypes = (String[])  args[index++];
            IBinder   resultTo      = (IBinder)   args[index++];
            Bundle    options       = (Bundle)    args[index];
            if (!ComponentUtils.isSelf(intents)) return method.invoke(who, args);
            for (Intent i : intents) i.setExtrasClassLoader(who.getClass().getClassLoader());
            return MundoCore.getBActivityManager().startActivities(
                    BActivityThread.getUserId(), intents, resolvedTypes, resultTo, options);
        }
    }

    @ProxyMethod("startIntentSenderForResult")
    public static class StartIntentSenderForResult extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("activityResumed")
    public static class ActivityResumed extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MundoCore.getBActivityManager().onActivityResumed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("activityDestroyed")
    public static class ActivityDestroyed extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MundoCore.getBActivityManager().onActivityDestroyed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("finishActivity")
    public static class FinishActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MundoCore.getBActivityManager().onFinishActivity((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getAppTasks")
    public static class GetAppTasks extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getCallingPackage")
    public static class getCallingPackage extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return MundoCore.getBActivityManager()
                    .getCallingPackage((IBinder) args[0], BActivityThread.getUserId());
        }
    }

    @ProxyMethod("getCallingActivity")
    public static class getCallingActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return MundoCore.getBActivityManager()
                    .getCallingActivity((IBinder) args[0], BActivityThread.getUserId());
        }
    }
}
