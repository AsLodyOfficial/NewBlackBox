package top.niunaijun.blackbox.fake.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import java.lang.reflect.Field;

import black.android.app.BRActivity;
import black.android.app.BRActivityThread;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.fake.hook.HookManager;
import top.niunaijun.blackbox.fake.hook.IInjectHook;
import top.niunaijun.blackbox.fake.service.HCallbackProxy;
import top.niunaijun.blackbox.fake.service.IActivityClientProxy;
import top.niunaijun.blackbox.utils.HackAppUtils;
import top.niunaijun.blackbox.utils.compat.ActivityCompat;
import top.niunaijun.blackbox.utils.compat.ActivityManagerCompat;
import top.niunaijun.blackbox.utils.compat.ContextCompat;

public final class AppInstrumentation extends BaseInstrumentationDelegate implements IInjectHook {

    private static final String TAG = AppInstrumentation.class.getSimpleName();

    private static AppInstrumentation sAppInstrumentation;

    public static AppInstrumentation get() {
        if (sAppInstrumentation == null) {
            synchronized (AppInstrumentation.class) {
                if (sAppInstrumentation == null) {
                    sAppInstrumentation = new AppInstrumentation();
                }
            }
        }
        return sAppInstrumentation;
    }

    public AppInstrumentation() {
    }

    @Override
    public void injectHook() {
        try {
            Instrumentation mInstrumentation = getCurrInstrumentation();
            if (mInstrumentation == this || checkInstrumentation(mInstrumentation))
                return;
            mBaseInstrumentation = (Instrumentation) mInstrumentation;
            BRActivityThread.get(BlackBoxCore.mainThread())._set_mInstrumentation(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instrumentation getCurrInstrumentation() {
        Object currentActivityThread = BlackBoxCore.mainThread();
        return BRActivityThread.get(currentActivityThread).mInstrumentation();
    }

    @Override
    public boolean isBadEnv() {
        return !checkInstrumentation(getCurrInstrumentation());
    }

    private boolean checkInstrumentation(Instrumentation instrumentation) {
        if (instrumentation instanceof AppInstrumentation) {
            return true;
        }
        Class<?> clazz = instrumentation.getClass();
        if (Instrumentation.class.equals(clazz)) {
            return false;
        }
        do {
            assert clazz != null;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Instrumentation.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        Object obj = field.get(instrumentation);
                        if ((obj instanceof AppInstrumentation)) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Instrumentation.class.equals(clazz));
        return false;
    }

    private void checkHCallback() {
        HookManager.get().checkEnv(HCallbackProxy.class);
    }

    private void checkActivity(Activity activity) {
        Log.d(TAG, "callActivityOnCreate: " + activity.getClass().getName());
        HackAppUtils.enableQQLogOutput(activity.getPackageName(), activity.getClassLoader());
        checkHCallback();
        HookManager.get().checkEnv(IActivityClientProxy.class);
        ActivityInfo info = BRActivity.get(activity).mActivityInfo();
        ContextCompat.fix(activity);
        ActivityCompat.fix(activity);
        if (info.theme != 0) {
            activity.getTheme().applyStyle(info.theme, true);
        }
        ActivityManagerCompat.setActivityOrientation(activity, info.screenOrientation);
        if (activity != null) {
            String actName = activity.getClass().getName();
            if (actName.contains("WebLogin") || actName.contains("login")) {
                for (String clsName : new String[]{
                        "com.tencent.imsdk.android.api.config.IMSDKConfig",
                        "com.itop.imsdk.android.api.config.IMSDKConfig"
                }) {
                    try {
                        Class<?> cfgClass = activity.getClassLoader().loadClass(clsName);
                        Field fImpl = cfgClass.getDeclaredField("mConfigImpl");
                        fImpl.setAccessible(true);
                        Object configImpl = fImpl.get(null);
                        if (configImpl != null) {
                            java.lang.reflect.Method putM = configImpl.getClass().getMethod("put", String.class, String.class);
                            putM.invoke(configImpl, "IMSDK_WEBVIEW_USE_CUSTOM_ERROR_PAGE", "false");
                            Log.i(TAG, "Successfully injected IMSDK_WEBVIEW_USE_CUSTOM_ERROR_PAGE=false into " + clsName);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ContextCompat.fix(context);

        return super.newApplication(cl, className, context);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        checkActivity(activity);
        super.callActivityOnCreate(activity, icicle, persistentState);
        BActivityThread.configureActivityWebViews(activity);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        checkActivity(activity);
        super.callActivityOnCreate(activity, icicle);
        BActivityThread.configureActivityWebViews(activity);
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        super.callActivityOnStart(activity);
        BActivityThread.configureActivityWebViews(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        BActivityThread.configureActivityWebViews(activity);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        checkHCallback();
        super.callApplicationOnCreate(app);
    }

    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            return super.newActivity(cl, className, intent);
        } catch (ClassNotFoundException e) {
            return mBaseInstrumentation.newActivity(cl, className, intent);
        }
    }
}
