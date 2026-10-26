package com.mundo.fake.delegate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.util.Log;

import com.mundo.env.com.cosmos.apm.framework.page.BRActivityLifeCycleHelper$ApplicationInstrumentation;
import java.lang.reflect.Field;

import com.mundo.env.android.app.BRActivity;
import com.mundo.env.android.app.BRActivityThread;

import com.mundo.MundoCore;
import com.mundo.app.BActivityThread;
import com.mundo.fake.hook.HookManager;
import com.mundo.fake.hook.IInjectHook;
import com.mundo.fake.service.HCallbackStub;
import com.mundo.fake.service.IActivityClientProxy;
import com.mundo.utils.HackAppUtils;
import com.mundo.utils.Slog;
import com.mundo.utils.compat.ActivityCompat;
import com.mundo.utils.compat.ActivityManagerCompat;
import com.mundo.utils.compat.ContextCompat;

/**
 * AppInstrumentation
 *
 * Wraps the real Instrumentation to hook the virtual application lifecycle.
 * All Facebook OAuth handling is done at the AMS Binder level (ActivityManagerCommonProxy).
 * This class only manages ClassLoader, theme, and orientation fixes for virtual activities.
 */
public final class AppInstrumentation extends BaseInstrumentationDelegate implements IInjectHook {

    private static final String TAG = AppInstrumentation.class.getSimpleName();
    private static final String RIFLE_APP_CLASS =
            "com.cosmos.apm.framework.page.ActivityLifeCycleHelper$ApplicationInstrumentation";

    private static AppInstrumentation sAppInstrumentation;
    private ClassLoader delegateAppClassLoader;

    private AppInstrumentation() {}

    public static AppInstrumentation get() {
        if (sAppInstrumentation == null) {
            synchronized (AppInstrumentation.class) {
                if (sAppInstrumentation == null) sAppInstrumentation = new AppInstrumentation();
            }
        }
        return sAppInstrumentation;
    }

    public ClassLoader getDelegateAppClassLoader() { return delegateAppClassLoader; }

    @Override
    public void injectHook() {
        try {
            Instrumentation curr = getCurrInstrumentation();
            if (curr == this || checkInstrumentation(curr)) return;
            mBaseInstrumentation = curr;
            BRActivityThread.get(MundoCore.mainThread())._set_mInstrumentation(this);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void fixRifleHook() {
        Instrumentation curr = getCurrInstrumentation();
        if (curr instanceof AppInstrumentation) return;
        if (RIFLE_APP_CLASS.equals(curr.getClass().getName())) {
            AppInstrumentation ai = (AppInstrumentation)
                    BRActivityLifeCycleHelper$ApplicationInstrumentation.get(curr).mBase();
            if (ai != null) mBaseInstrumentation = ai.mBaseInstrumentation;
            BRActivityThread.get(MundoCore.mainThread())._set_mInstrumentation(this);
        }
    }

    public void fixInstrumentationAfterApplicationOnCreate() {
        Instrumentation curr = getCurrInstrumentation();
        if (curr instanceof AppInstrumentation) return;
        for (Field f : curr.getClass().getDeclaredFields()) {
            if (!Instrumentation.class.isAssignableFrom(f.getType())) continue;
            f.setAccessible(true);
            try {
                Object obj = f.get(curr);
                if (obj instanceof AppInstrumentation) {
                    Instrumentation base = ((AppInstrumentation) obj).mBaseInstrumentation;
                    f.set(curr, base);
                    ((AppInstrumentation) obj).mBaseInstrumentation = curr;
                    BRActivityThread.get(MundoCore.mainThread())._set_mInstrumentation(obj);
                }
            } catch (Exception ignored) {}
            break;
        }
    }

    public Instrumentation getCurrInstrumentation() {
        return BRActivityThread.get(MundoCore.mainThread()).mInstrumentation();
    }

    @Override
    public boolean isBadEnv() { return !checkInstrumentation(getCurrInstrumentation()); }

    private boolean checkInstrumentation(Instrumentation ins) {
        if (ins instanceof AppInstrumentation) return true;
        Class<?> cls = ins.getClass();
        if (Instrumentation.class.equals(cls)) return false;
        do {
            for (Field f : cls.getDeclaredFields()) {
                if (!Instrumentation.class.isAssignableFrom(f.getType())) continue;
                f.setAccessible(true);
                try { if (f.get(ins) instanceof AppInstrumentation) return true; }
                catch (Exception e) { return false; }
            }
            cls = cls.getSuperclass();
        } while (!Instrumentation.class.equals(cls));
        return false;
    }

    private void checkHCallback() { HookManager.get().checkEnv(HCallbackStub.class); }

    private void checkActivity(Activity activity) {
        HackAppUtils.enableQQLogOutput(activity.getPackageName(), activity.getClassLoader());
        checkHCallback();
        HookManager.get().checkEnv(IActivityClientProxy.class);
        ActivityInfo info = BRActivity.get(activity).mActivityInfo();
        ContextCompat.fix(activity);
        ActivityCompat.fix(activity);
        if (info.theme != 0) activity.getTheme().applyStyle(info.theme, true);
        ActivityManagerCompat.setActivityOrientation(activity, info.screenOrientation);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context ctx)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ContextCompat.fix(ctx);
        BActivityThread.currentActivityThread().loadXposed(ctx);
        delegateAppClassLoader = ctx.getClassLoader();
        return super.newApplication(cl, className, ctx);
    }

    @Override
    public void callActivityOnCreate(Activity a, Bundle b, PersistableBundle p) {
        checkActivity(a); super.callActivityOnCreate(a, b, p);
    }

    @Override
    public void callActivityOnCreate(Activity a, Bundle b) {
        checkActivity(a); super.callActivityOnCreate(a, b);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        checkHCallback(); super.callApplicationOnCreate(app);
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try { return super.newActivity(cl, className, intent); }
        catch (ClassNotFoundException e) { return mBaseInstrumentation.newActivity(cl, className, intent); }
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
    }

    @Override
    public ActivityResult execStartActivity(Context ctx, IBinder b0, IBinder b1,
            Activity activity, Intent intent, int i, Bundle bundle) throws Throwable {
        return super.execStartActivity(ctx, b0, b1, activity, intent, i, bundle);
    }

    @Override
    public ActivityResult execStartActivity(Context ctx, IBinder b0, IBinder b1,
            android.app.Fragment fragment, Intent intent, int i) throws Throwable {
        return super.execStartActivity(ctx, b0, b1, fragment, intent, i);
    }

    @Override
    public ActivityResult execStartActivity(Context ctx, IBinder b0, IBinder b1,
            android.app.Fragment fragment, Intent intent, int i, Bundle bundle) throws Throwable {
        return super.execStartActivity(ctx, b0, b1, fragment, intent, i, bundle);
    }

    @Override
    public ActivityResult execStartActivity(Context ctx, IBinder b0, IBinder b1,
            String str, Intent intent, int i, Bundle bundle) throws Throwable {
        return super.execStartActivity(ctx, b0, b1, str, intent, i, bundle);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public ActivityResult execStartActivity(Context ctx, IBinder b0, IBinder b1,
            Activity activity, Intent intent, int i, Bundle bundle, UserHandle uh) throws Throwable {
        return super.execStartActivity(ctx, b0, b1, activity, intent, i, bundle, uh);
    }
}
