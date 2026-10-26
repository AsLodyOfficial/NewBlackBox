package com.mundo.fake.service;

import android.os.IInterface;

import java.lang.reflect.Method;

import com.mundo.env.android.os.BRServiceManager;
import com.mundo.env.android.view.BRIWindowManagerStub;
import com.mundo.env.android.view.BRWindowManagerGlobal;
import com.mundo.MundoCore;
import com.mundo.fake.hook.BinderInvocationStub;
import com.mundo.fake.hook.MethodHook;
import com.mundo.fake.hook.ProxyMethod;
import com.mundo.utils.MethodParameterUtils;

/**
 * Created by @officialakshit01 on 4/6/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶 つ０
 * しーＪ
 * 此处无Bug
 */
public class IWindowManagerProxy extends BinderInvocationStub {
    public static final String TAG = "WindowManagerStub";

    public IWindowManagerProxy() {
        super(BRServiceManager.get().getService("window"));
    }

    @Override
    protected Object getWho() {
        return BRIWindowManagerStub.get().asInterface(BRServiceManager.get().getService("window"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("window");
        BRWindowManagerGlobal.get()._set_sWindowManagerService(null);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("openSession")
    public static class OpenSession extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface session = (IInterface) method.invoke(who, args);
            IWindowSessionProxy IWindowSessionProxy = new IWindowSessionProxy(session);
            IWindowSessionProxy.injectHook();
            return IWindowSessionProxy.getProxyInvocation();
        }
    }
}
