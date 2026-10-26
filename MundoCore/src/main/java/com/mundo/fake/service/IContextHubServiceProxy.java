package com.mundo.fake.service;


import com.mundo.env.android.hardware.location.BRIContextHubServiceStub;
import com.mundo.env.android.os.BRServiceManager;
import com.mundo.fake.hook.BinderInvocationStub;
import com.mundo.fake.service.base.ValueMethodProxy;
import com.mundo.utils.compat.BuildCompat;

/**
 * Created by BlackBox on 2022/3/2.
 */
public class IContextHubServiceProxy extends BinderInvocationStub {

    public IContextHubServiceProxy() {
        super(BRServiceManager.get().getService(getServiceName()));
    }

    private static String getServiceName() {
        return BuildCompat.isOreo() ? "contexthub" : "contexthub_service";
    }

    @Override
    protected Object getWho() {
        android.os.IBinder binder = BRServiceManager.get().getService(getServiceName());
        if (binder == null) {
            return null;
        }
        return BRIContextHubServiceStub.get().asInterface(binder);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(getServiceName());
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("registerCallback", 0));
        addMethodHook(new ValueMethodProxy("getContextHubInfo", null));
        addMethodHook(new ValueMethodProxy("getContextHubHandles",new int[]{}));
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
