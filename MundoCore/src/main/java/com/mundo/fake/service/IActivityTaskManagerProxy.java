package com.mundo.fake.service;

import android.app.ActivityManager;

import java.lang.reflect.Method;

import com.mundo.env.android.app.BRActivityTaskManager;
import com.mundo.env.android.app.BRIActivityTaskManagerStub;
import com.mundo.env.android.os.BRServiceManager;
import com.mundo.env.android.util.BRSingleton;

import com.mundo.fake.hook.BinderInvocationStub;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.fake.hook.ScanClass;
import com.mundo.utils.compat.TaskDescriptionCompat;

/**
 * Created by @officialakshit01
 * Android 10 (API 29) -> Android 16 (API 35+)
 * Fully compatible & crash-safe
 */
@ScanClass(ActivityManagerCommonProxy.class)
public class IActivityTaskManagerProxy extends BinderInvocationStub {

    public static final String TAG = "ActivityTaskManager";

    public IActivityTaskManagerProxy() {
        super(BRServiceManager.get().getService("activity_task"));
    }

    @Override
    protected Object getWho() {
        return BRIActivityTaskManagerStub.get().asInterface(BRServiceManager.get().getService("activity_task"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("activity_task");
        BRActivityTaskManager.get().getService();
        Object singleton = BRActivityTaskManager.get().IActivityTaskManagerSingleton();
        BRSingleton.get(singleton)._set_mInstance(BRIActivityTaskManagerStub.get().asInterface(this));
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("setTaskDescription")
    public static class SetTaskDescription extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ActivityManager.TaskDescription) {
                        ActivityManager.TaskDescription td = (ActivityManager.TaskDescription) args[i];
                        args[i] = TaskDescriptionCompat.fix(td);
                        break;
                    }
                }
            }

            return method.invoke(who, args);
        }
    }
}