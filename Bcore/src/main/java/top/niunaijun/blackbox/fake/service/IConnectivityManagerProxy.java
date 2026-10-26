package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Network;
import android.net.LinkProperties;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import black.android.net.BRIConnectivityManagerStub;
import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.ScanClass;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


@ScanClass(VpnCommonProxy.class)
public class IConnectivityManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IConnectivityManagerProxy";

    public IConnectivityManagerProxy() {
        super(BRServiceManager.get().getService(Context.CONNECTIVITY_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIConnectivityManagerStub.get().asInterface(BRServiceManager.get().getService(Context.CONNECTIVITY_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        top.niunaijun.blackbox.utils.MethodParameterUtils.replaceAllAppPkg(args);
        if (args != null && android.os.Build.VERSION.SDK_INT >= 31) {
            try {
                Object hostAttr = top.niunaijun.blackbox.BlackBoxCore.getContext().getAttributionSource();
                if (hostAttr != null) {
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] != null && "android.content.AttributionSource".equals(args[i].getClass().getName())) {
                            args[i] = hostAttr;
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return super.invoke(proxy, method, args);
    }

    private static void ensureNetworkInfoConnected(Object networkInfo) {
        if (networkInfo == null) return;
        try {
            Method setIsAvailable = networkInfo.getClass().getDeclaredMethod("setIsAvailable", boolean.class);
            setIsAvailable.setAccessible(true);
            setIsAvailable.invoke(networkInfo, true);
        } catch (Throwable ignored) {
            try {
                java.lang.reflect.Field f = networkInfo.getClass().getDeclaredField("mIsAvailable");
                f.setAccessible(true);
                f.setBoolean(networkInfo, true);
            } catch (Throwable ignored2) {}
        }
        try {
            Method setDetailedState = networkInfo.getClass().getDeclaredMethod("setDetailedState",
                    NetworkInfo.DetailedState.class, String.class, String.class);
            setDetailedState.setAccessible(true);
            setDetailedState.invoke(networkInfo, NetworkInfo.DetailedState.CONNECTED, null, "connected");
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Field stateField = networkInfo.getClass().getDeclaredField("mState");
            stateField.setAccessible(true);
            stateField.set(networkInfo, NetworkInfo.State.CONNECTED);
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Field detailedField = networkInfo.getClass().getDeclaredField("mDetailedState");
            detailedField.setAccessible(true);
            detailedField.set(networkInfo, NetworkInfo.DetailedState.CONNECTED);
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Field availField = networkInfo.getClass().getDeclaredField("mIsAvailable");
            availField.setAccessible(true);
            availField.setBoolean(networkInfo, true);
        } catch (Throwable ignored) {}
    }

    private static Object createNetworkInfo(int type, int subType, String typeName, String subTypeName) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                NetworkInfo networkInfo = new NetworkInfo(type, subType, typeName, subTypeName);
                ensureNetworkInfoConnected(networkInfo);
                return networkInfo;
            } else {
                try {
                    Class<?> networkInfoClass = Class.forName("android.net.NetworkInfo");
                    Constructor<?> constructor = networkInfoClass.getDeclaredConstructor(int.class, int.class, String.class, String.class);
                    constructor.setAccessible(true);
                    Object networkInfo = constructor.newInstance(type, subType, typeName, subTypeName);
                    ensureNetworkInfoConnected(networkInfo);
                    return networkInfo;
                } catch (Exception e) {
                    Slog.w(TAG, "Failed to create NetworkInfo via reflection: " + e.getMessage());
                    return null;
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Failed to create NetworkInfo: " + e.getMessage());
            return null;
        }
    }

    private static Object createNetworkInfoArray() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                NetworkInfo wifi = new NetworkInfo(ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");
                ensureNetworkInfoConnected(wifi);
                NetworkInfo mobile = new NetworkInfo(ConnectivityManager.TYPE_MOBILE, 0, "MOBILE", "LTE");
                ensureNetworkInfoConnected(mobile);
                return new NetworkInfo[] { wifi, mobile };
            } else {
                try {
                    Class<?> networkInfoClass = Class.forName("android.net.NetworkInfo");
                    Constructor<?> constructor = networkInfoClass.getDeclaredConstructor(int.class, int.class, String.class, String.class);
                    constructor.setAccessible(true);
                    Object wifi = constructor.newInstance(ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");
                    ensureNetworkInfoConnected(wifi);
                    Object mobile = constructor.newInstance(ConnectivityManager.TYPE_MOBILE, 0, "MOBILE", "LTE");
                    ensureNetworkInfoConnected(mobile);
                    Object[] networkInfoArray = (Object[]) java.lang.reflect.Array.newInstance(networkInfoClass, 2);
                    java.lang.reflect.Array.set(networkInfoArray, 0, wifi);
                    java.lang.reflect.Array.set(networkInfoArray, 1, mobile);
                    return networkInfoArray;
                } catch (Exception e) {
                    Slog.w(TAG, "Failed to create NetworkInfo array via reflection: " + e.getMessage());
                    return new Object[0];
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Failed to create fallback NetworkInfo array: " + e.getMessage());
            return new Object[0];
        }
    }

    private static void enhanceCapabilities(Object nc) {
        if (nc == null) return;
        try {
            Method addTransportTypeMethod = nc.getClass().getDeclaredMethod("addTransportType", int.class);
            addTransportTypeMethod.setAccessible(true);
            addTransportTypeMethod.invoke(nc, android.net.NetworkCapabilities.TRANSPORT_WIFI);
            addTransportTypeMethod.invoke(nc, android.net.NetworkCapabilities.TRANSPORT_CELLULAR);
        } catch (Throwable ignored) {
            try {
                java.lang.reflect.Field f = nc.getClass().getDeclaredField("mTransportTypes");
                f.setAccessible(true);
                long mask = f.getLong(nc);
                mask |= (1L << android.net.NetworkCapabilities.TRANSPORT_WIFI)
                      | (1L << android.net.NetworkCapabilities.TRANSPORT_CELLULAR);
                f.setLong(nc, mask);
            } catch (Throwable ignored2) {}
        }
        try {
            Method addCapabilityMethod = nc.getClass().getDeclaredMethod("addCapability", int.class);
            addCapabilityMethod.setAccessible(true);
            addCapabilityMethod.invoke(nc, android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
            addCapabilityMethod.invoke(nc, android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            addCapabilityMethod.invoke(nc, android.net.NetworkCapabilities.NET_CAPABILITY_TRUSTED);
            addCapabilityMethod.invoke(nc, android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
            addCapabilityMethod.invoke(nc, android.net.NetworkCapabilities.NET_CAPABILITY_NOT_VPN);
            addCapabilityMethod.invoke(nc, android.net.NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED);
        } catch (Throwable ignored) {
            try {
                java.lang.reflect.Field f = nc.getClass().getDeclaredField("mCapabilities");
                f.setAccessible(true);
                long mask = f.getLong(nc);
                mask |= (1L << android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                      | (1L << android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                      | (1L << android.net.NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                      | (1L << android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
                f.setLong(nc, mask);
            } catch (Throwable ignored2) {}
        }
    }

    private static Object createNetworkCapabilities() {
        try {
            Class<?> networkCapabilitiesClass = Class.forName("android.net.NetworkCapabilities");
            Constructor<?> constructor = networkCapabilitiesClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object nc = constructor.newInstance();
            enhanceCapabilities(nc);
            return nc;
        } catch (Exception e) {
            Slog.w(TAG, "Failed to create NetworkCapabilities via reflection: " + e.getMessage());
            return null;
        }
    }

    
    private static Object createLinkProperties() {
        try {
            Class<?> linkPropertiesClass = Class.forName("android.net.LinkProperties");
            Constructor<?> constructor = linkPropertiesClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object linkProperties = constructor.newInstance();

            try {
                Method addDnsServer = linkProperties.getClass().getMethod("addDnsServer", java.net.InetAddress.class);
                addDnsServer.setAccessible(true);
                addDnsServer.invoke(linkProperties, java.net.InetAddress.getByName("8.8.8.8"));
                addDnsServer.invoke(linkProperties, java.net.InetAddress.getByName("8.8.4.4"));
                addDnsServer.invoke(linkProperties, java.net.InetAddress.getByName("1.1.1.1"));
            } catch (Throwable e) {
                Slog.w(TAG, "Could not add DNS servers: " + e.getMessage());
            }

            try {
                Method setInterfaceName = linkProperties.getClass().getMethod("setInterfaceName", String.class);
                setInterfaceName.setAccessible(true);
                setInterfaceName.invoke(linkProperties, "wlan0");
            } catch (Throwable ignored) {}

            return linkProperties;
        } catch (Exception e) {
            Slog.w(TAG, "Failed to create LinkProperties via reflection: " + e.getMessage());
            return null;
        }
    }



    @ProxyMethod("getNetworkInfo")
    public static class GetNetworkInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Object result = method.invoke(who, args);
                if (result != null) {
                    ensureNetworkInfoConnected(result);
                    return result;
                }
                return createNetworkInfo(ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");
            } catch (Exception e) {
                return createNetworkInfo(ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");
            }
        }
    }

    @ProxyMethod("getAllNetworkInfo")
    public static class GetAllNetworkInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Object result = method.invoke(who, args);
                if (result != null && Array.getLength(result) > 0) {
                    int len = Array.getLength(result);
                    for (int i = 0; i < len; i++) {
                        ensureNetworkInfoConnected(Array.get(result, i));
                    }
                    return result;
                }
                return createNetworkInfoArray();
            } catch (Exception e) {
                return createNetworkInfoArray();
            }
        }

        private Object createNetworkInfoArray() {
            try {
                
                return IConnectivityManagerProxy.createNetworkInfoArray();
            } catch (Exception e) {
                Slog.e(TAG, "Failed to create fallback NetworkInfo array: " + e.getMessage());
                return new Object[0];
            }
        }
    }

    
    @ProxyMethod("getAllNetworks")
    public static class GetAllNetworks extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                
                Object result = method.invoke(who, args);
                if (result != null) {
                    
                    if (Array.getLength(result) > 0) {
                         return result;
                    }
                }
            } catch (Exception e) {
                
            }

            Slog.d(TAG, "Creating fallback Network[] for getAllNetworks");
            
            
            try {
                 
                 Method getActiveNetworkMethod = null;
                 try {
                     getActiveNetworkMethod = who.getClass().getMethod("getActiveNetwork");
                 } catch (NoSuchMethodException e) {
                     
                     for (Method m : who.getClass().getMethods()) {
                         if (m.getName().equals("getActiveNetwork")) {
                             getActiveNetworkMethod = m;
                             break;
                         }
                     }
                 }

                 if (getActiveNetworkMethod != null) {
                     Object activeNetwork = getActiveNetworkMethod.invoke(who);
                     if (activeNetwork != null) {
                         Class<?> networkClass = activeNetwork.getClass();
                         Object networkArray = Array.newInstance(networkClass, 1);
                         Array.set(networkArray, 0, activeNetwork);
                         Slog.d(TAG, "Refilled getAllNetworks with Active Network: " + activeNetwork);
                         return networkArray;
                     }
                 }
            } catch (Exception e) {
                 Slog.w(TAG, "Failed to use Active Network for fallback: " + e.getMessage());
            }

            
            try {
                Class<?> networkClass = Class.forName("android.net.Network");
                Object networkArray = Array.newInstance(networkClass, 1);
                
                
                
                Constructor<?> constructor = networkClass.getConstructor(int.class);
                Object network = constructor.newInstance(1);
                
                Array.set(networkArray, 0, network);
                return networkArray;
            } catch (Exception e) {
                 Slog.w(TAG, "Failed to create fallback Network[]: " + e.getMessage());
                 return method.invoke(who, args);
            }
        }
    }

    
    @ProxyMethod("getNetworkCapabilities")
    public static class GetNetworkCapabilities extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                try {
                    Object result = method.invoke(who, args);
                    if (result == null && args != null && args.length > 0) {
                        try {
                            Method getActiveNet = who.getClass().getMethod("getActiveNetwork");
                            Object activeNet = getActiveNet.invoke(who);
                            if (activeNet != null) {
                                args[0] = activeNet;
                                result = method.invoke(who, args);
                            }
                        } catch (Throwable ignored) {}
                    }
                    if (result != null) {
                        enhanceCapabilities(result);
                        return result;
                    }
                    Object nc = IConnectivityManagerProxy.createNetworkCapabilities();
                    if (nc != null) {
                        return nc;
                    }
                } catch (Throwable e) {
                    Object nc = IConnectivityManagerProxy.createNetworkCapabilities();
                    if (nc != null) {
                        return nc;
                    }
                }
            }
            return method.invoke(who, args);
        }
    }

    
    @ProxyMethod("getActiveNetwork")
    public static class GetActiveNetwork extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                try {
                    
                    Object result = method.invoke(who, args);
                    if (result != null) {
                        return result;
                    }

                    
                    
                    android.net.Network network;
                    try {
                        
                        Constructor<android.net.Network> constructor = android.net.Network.class.getConstructor(int.class);
                        network = constructor.newInstance(1);
                    } catch (Exception e) {
                        
                        try {
                            Constructor<android.net.Network> defaultConstructor = android.net.Network.class.getDeclaredConstructor();
                            defaultConstructor.setAccessible(true);
                            network = defaultConstructor.newInstance();
                        } catch (Exception e2) {
                            
                            Slog.w(TAG, "Could not create Network object, falling back to original method");
                            return method.invoke(who, args);
                        }
                    }
                    Slog.d(TAG, "Created mock Network object for sandboxed app (fallback)");
                    return network;
                } catch (Exception e) {
                    Slog.w(TAG, "Error creating Network object: " + e.getMessage());
                }
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getActiveNetworkInfo")
    public static class GetActiveNetworkInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Object result = method.invoke(who, args);
                if (result != null) {
                    ensureNetworkInfoConnected(result);
                    return result;
                }
            } catch (Exception ignored) {
            }
            return createNetworkInfo(ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");
        }
    }

    
    @ProxyMethod("getLinkProperties")
    public static class GetLinkProperties extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                try {
                    Object result = method.invoke(who, args);
                    if (result == null && args != null && args.length > 0) {
                        try {
                            Method getActiveNet = who.getClass().getMethod("getActiveNetwork");
                            Object activeNet = getActiveNet.invoke(who);
                            if (activeNet != null) {
                                args[0] = activeNet;
                                result = method.invoke(who, args);
                            }
                        } catch (Throwable ignored) {}
                    }
                    if (result != null) {
                        try {
                            Method getDns = result.getClass().getMethod("getDnsServers");
                            java.util.List<?> dnsList = (java.util.List<?>) getDns.invoke(result);
                            if (dnsList == null || dnsList.isEmpty()) {
                                Method addDns = result.getClass().getMethod("addDnsServer", java.net.InetAddress.class);
                                addDns.setAccessible(true);
                                addDns.invoke(result, java.net.InetAddress.getByName("8.8.8.8"));
                                addDns.invoke(result, java.net.InetAddress.getByName("8.8.4.4"));
                            }
                        } catch (Throwable ignored) {}
                        return result;
                    }

                    Object linkProperties = IConnectivityManagerProxy.createLinkProperties();
                    if (linkProperties != null) {
                        return linkProperties;
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Error creating LinkProperties: " + e.getMessage());
                }
            }
            return method.invoke(who, args);
        }
    }

    
    @ProxyMethod("getPrivateDnsServerName")
    public static class GetPrivateDnsServerName extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "Disabling private DNS for sandboxed app");
            return null;
        }
    }

    
    @ProxyMethod("isPrivateDnsActive")
    public static class IsPrivateDnsActive extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "Private DNS disabled for sandboxed app");
            return false;
        }
    }

    
    @ProxyMethod("getDnsServers")
    public static class GetDnsServers extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                
                java.util.List<java.net.InetAddress> dnsServers = new java.util.ArrayList<>();
                dnsServers.add(java.net.InetAddress.getByName("8.8.8.8"));
                dnsServers.add(java.net.InetAddress.getByName("8.8.4.4"));
                Slog.d(TAG, "Returning system DNS servers for sandboxed app");
                return dnsServers;
            } catch (Exception e) {
                Slog.w(TAG, "Error creating DNS servers list: " + e.getMessage());
                return method.invoke(who, args);
            }
        }
    }

    
    @ProxyMethod("isNetworkValidated")
    public static class IsNetworkValidated extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "Network validation enabled for sandboxed app");
            return true;
        }
    }

    
    @ProxyMethod("requestNetwork")
    public static class RequestNetwork extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Slog.d(TAG, "Intercepting requestNetwork call for internet access");
            try {
                
                Object result = method.invoke(who, args);
                if (result != null) {
                    Slog.d(TAG, "requestNetwork succeeded via original method");
                    return result;
                }
                
                
                Slog.w(TAG, "requestNetwork failed, creating fallback result");
                return createMockNetworkRequestResult();
                
            } catch (Exception e) {
                Slog.w(TAG, "Error in requestNetwork, creating fallback: " + e.getMessage());
                return createMockNetworkRequestResult();
            }
        }
        
        private Object createMockNetworkRequestResult() {
            try {
                
                
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    
                    Class<?> networkRequestClass = Class.forName("android.net.NetworkRequest");
                    if (networkRequestClass != null) {
                        Slog.d(TAG, "Created fallback NetworkRequest for internet access");
                        return null; 
                    }
                }
            } catch (Exception e) {
                Slog.w(TAG, "Could not create NetworkRequest fallback: " + e.getMessage());
            }
            return null;
        }
    }

    
    @ProxyMethod("registerNetworkCallback")
    public static class RegisterNetworkCallback extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Slog.d(TAG, "Intercepting registerNetworkCallback for internet access");
            try {
                
                Object result = method.invoke(who, args);
                Slog.d(TAG, "Network callback registration successful");
                return result;
            } catch (Exception e) {
                Slog.w(TAG, "Network callback registration failed: " + e.getMessage());
                
                return 0;
            }
        }
    }

    
    @ProxyMethod("registerDefaultNetworkCallback")
    public static class RegisterDefaultNetworkCallback extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Slog.d(TAG, "Intercepting registerDefaultNetworkCallback for internet access");
            try {
                
                Object result = method.invoke(who, args);
                Slog.d(TAG, "Default network callback registration successful");
                return result;
            } catch (Exception e) {
                Slog.w(TAG, "Default network callback registration failed: " + e.getMessage());
                
                return 0;
            }
        }
    }

    
    @ProxyMethod("getActiveNetworkInfoForUid")
    public static class GetActiveNetworkInfoForUid extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Object result = method.invoke(who, args);
                if (result != null) {
                    ensureNetworkInfoConnected(result);
                    return result;
                }
            } catch (Exception ignored) {
            }
            return createNetworkInfo(ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");
        }
    }

    @ProxyMethod("addDefaultNetworkActiveListener")
    public static class AddDefaultNetworkActiveListener extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, args);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    @ProxyMethod("removeDefaultNetworkActiveListener")
    public static class RemoveDefaultNetworkActiveListener extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, args);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    @ProxyMethod("isActiveNetworkMetered")
    public static class IsActiveNetworkMetered extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Object result = method.invoke(who, args);
                if (result != null) {
                    return result;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }
    }

    @ProxyMethod("getNetworkForType")
    public static class GetNetworkForType extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Object result = method.invoke(who, args);
                if (result != null) {
                    return result;
                }
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    try {
                        Constructor<android.net.Network> constructor = android.net.Network.class.getConstructor(int.class);
                        return constructor.newInstance(1);
                    } catch (Exception ignored) {}
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    @ProxyMethod("unregisterNetworkCallback")
    public static class UnregisterNetworkCallback extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, args);
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
