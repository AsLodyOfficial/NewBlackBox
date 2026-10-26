package top.niunaijun.blackbox.fake.service;

import android.os.IBinder;
import android.util.Log;
import java.lang.reflect.Method;

import black.android.os.BRServiceManager;
import black.android.telephony.BRTelephonyManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;


public class IPhoneSubInfoProxy extends BinderInvocationStub {
    public static final String TAG = "IPhoneSubInfoProxy";

    public IPhoneSubInfoProxy() {
        super(BRServiceManager.get().getService("iphonesubinfo"));
        try {
            if (BRTelephonyManager.get()._check_sServiceHandleCacheEnabled() != null) {
                BRTelephonyManager.get()._set_sServiceHandleCacheEnabled(true);
            }
            if (BRTelephonyManager.get()._check_getSubscriberInfoService() != null) {
                BRTelephonyManager.get().getSubscriberInfoService();
            }
        } catch (Throwable ignored) {}
    }

    @Override
    protected Object getWho() {
        try {
            IBinder binder = BRServiceManager.get().getService("iphonesubinfo");
            if (binder != null) {
                Class<?> stubClass = Class.forName("com.android.internal.telephony.IPhoneSubInfo$Stub");
                Method asInterface = stubClass.getMethod("asInterface", IBinder.class);
                return asInterface.invoke(null, binder);
            }
        } catch (Throwable t) {
            Log.w(TAG, "getWho failed from ServiceManager: " + t.getMessage());
        }
        try {
            return BRTelephonyManager.get().sIPhoneSubInfo();
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("iphonesubinfo");
        try {
            if (BRTelephonyManager.get()._check_sIPhoneSubInfo() != null) {
                BRTelephonyManager.get()._set_sIPhoneSubInfo(proxyInvocation);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return super.invoke(proxy, method, args);
        } catch (Throwable t) {
            android.util.Log.w(TAG, "IPhoneSubInfoProxy caught exception in " + method.getName() + ", returning safe default: " + t.getMessage());
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(String.class)) {
                return null;
            }
            if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                return 0;
            }
            if (returnType.equals(long.class) || returnType.equals(Long.class)) {
                return 0L;
            }
            if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                return false;
            }
            return null;
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getIccSerialNumber")
    public static class GetIccSerialNumber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getIccSerialNumberForSubscriber")
    public static class GetIccSerialNumberForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getSubscriberId")
    public static class GetSubscriberId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getSubscriberIdForSubscriber")
    public static class GetSubscriberIdForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getDeviceId")
    public static class GetDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getDeviceIdForPhone")
    public static class GetDeviceIdForPhone extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getImeiForSubscriber")
    public static class GetImeiForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getMeidForSubscriber")
    public static class GetMeidForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getDeviceSvnUsingSubId")
    public static class GetDeviceSvnUsingSubId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getNaiForSubscriber")
    public static class GetNaiForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getLine1Number")
    public static class GetLine1Number extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getLine1NumberForSubscriber")
    public static class getLine1NumberForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getLine1AlphaTag")
    public static class GetLine1AlphaTag extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getLine1AlphaTagForSubscriber")
    public static class GetLine1AlphaTagForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getMsisdn")
    public static class GetMsisdn extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getMsisdnForSubscriber")
    public static class GetMsisdnForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getVoiceMailNumber")
    public static class GetVoiceMailNumber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getVoiceMailNumberForSubscriber")
    public static class GetVoiceMailNumberForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getVoiceMailAlphaTag")
    public static class GetVoiceMailAlphaTag extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getVoiceMailAlphaTagForSubscriber")
    public static class GetVoiceMailAlphaTagForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }
}
