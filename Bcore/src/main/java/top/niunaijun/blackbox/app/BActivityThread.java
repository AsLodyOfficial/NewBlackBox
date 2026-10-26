package top.niunaijun.blackbox.app;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import android.view.View;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import black.android.app.ActivityThreadAppBindDataContext;
import black.android.app.BRActivity;
import black.android.app.BRActivityManagerNative;
import black.android.app.BRActivityThread;
import black.android.app.BRActivityThreadActivityClientRecord;
import black.android.app.BRActivityThreadAppBindData;
import black.android.app.BRActivityThreadNMR1;
import black.android.app.BRActivityThreadQ;
import black.android.app.BRContextImpl;
import black.android.app.BRLoadedApk;
import black.android.app.BRService;
import black.android.app.LoadedApk;
import black.android.content.BRBroadcastReceiver;
import black.android.content.BRContentProviderClient;
import black.android.graphics.BRCompatibility;
import black.android.security.net.config.BRNetworkSecurityConfigProvider;
import black.com.android.internal.content.BRReferrerIntent;
import black.dalvik.system.BRVMRuntime;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.dispatcher.AppServiceDispatcher;
import top.niunaijun.blackbox.core.CrashHandler;
import top.niunaijun.blackbox.core.IBActivityThread;
import top.niunaijun.blackbox.core.IOCore;
import top.niunaijun.blackbox.core.NativeCore;
import top.niunaijun.blackbox.core.env.VirtualRuntime;
import top.niunaijun.blackbox.core.system.user.BUserHandle;
import top.niunaijun.blackbox.entity.AppConfig;
import top.niunaijun.blackbox.entity.am.ReceiverData;

import top.niunaijun.blackbox.fake.delegate.AppInstrumentation;
import top.niunaijun.blackbox.fake.delegate.ContentProviderDelegate;

import top.niunaijun.blackbox.fake.hook.HookManager;
import top.niunaijun.blackbox.fake.service.HCallbackProxy;
import top.niunaijun.blackbox.utils.Reflector;
import top.niunaijun.blackbox.utils.SafeContextWrapper;
import top.niunaijun.blackbox.utils.GlobalContextWrapper;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.utils.compat.ActivityManagerCompat;
import top.niunaijun.blackbox.utils.compat.BuildCompat;
import top.niunaijun.blackbox.utils.compat.ContextCompat;
import top.niunaijun.blackbox.utils.compat.StrictModeCompat;
import top.niunaijun.blackbox.core.system.JarManager;


public class BActivityThread extends IBActivityThread.Stub {
    public static final String TAG = "BActivityThread";

    private static BActivityThread sBActivityThread;
    private AppBindData mBoundApplication;
    private Application mInitialApplication;
    private volatile AppConfig mAppConfig;
    private final List<ProviderInfo> mProviders = new ArrayList<>();
    private final Handler mH = BlackBoxCore.get().getHandler();
    private static final Object mConfigLock = new Object();

    public static boolean isThreadInit() {
        return sBActivityThread != null;
    }

    public static BActivityThread currentActivityThread() {
        if (sBActivityThread == null) {
            synchronized (BActivityThread.class) {
                if (sBActivityThread == null) {
                    sBActivityThread = new BActivityThread();
                }
            }
        }
        return sBActivityThread;
    }

    public static AppConfig getAppConfig() {
        return currentActivityThread().mAppConfig;
    }

    public static List<ProviderInfo> getProviders() {
        return currentActivityThread().mProviders;
    }

    public static String getAppProcessName() {
        AppConfig config = getAppConfig();
        if (config != null) {
            return config.processName;
        } else if (currentActivityThread().mBoundApplication != null) {
            return currentActivityThread().mBoundApplication.processName;
        } else {
            return null;
        }
    }

    public static String getAppPackageName() {
        AppConfig config = getAppConfig();
        if (config != null) {
            return config.packageName;
        } else if (currentActivityThread().mInitialApplication != null) {
            return currentActivityThread().mInitialApplication.getPackageName();
        } else {
            return null;
        }
    }

    public static Application getApplication() {
        return currentActivityThread().mInitialApplication;
    }

    public static int getAppPid() {
        AppConfig config = getAppConfig();
        return config == null ? -1 : config.bpid;
    }

    public static int getBUid() {
        AppConfig config = getAppConfig();
        return config == null ? BUserHandle.AID_APP_START : config.buid;
    }

    public static int getBAppId() {
        return BUserHandle.getAppId(getBUid());
    }

    public static int getCallingBUid() {
        AppConfig config = getAppConfig();
        return config == null ? BlackBoxCore.getHostUid() : config.callingBUid;
    }

    public static int getUid() {
        AppConfig config = getAppConfig();
        return config == null ? -1 : config.uid;
    }

    public static int getUserId() {
        AppConfig config = getAppConfig();
        return config == null ? 0 : config.userId;
    }

    public void initProcess(AppConfig appConfig) {
        synchronized (mConfigLock) {
            if (this.mAppConfig != null && !this.mAppConfig.packageName.equals(appConfig.packageName)) {
                
                throw new RuntimeException("reject init process: " + appConfig.processName + ", this process is : " + this.mAppConfig.processName);
            }
            this.mAppConfig = appConfig;
            IBinder iBinder = asBinder();
            try {
                iBinder.linkToDeath(new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        synchronized (mConfigLock) {
                            try {
                                iBinder.linkToDeath(this, 0);
                            } catch (RemoteException ignored) {
                            }
                            mAppConfig = null;
                        }
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isInit() {
        return mBoundApplication != null;
    }

    public Service createService(ServiceInfo serviceInfo, IBinder token) {
        if (!BActivityThread.currentActivityThread().isInit()) {
            BActivityThread.currentActivityThread().bindApplication(serviceInfo.packageName, serviceInfo.processName);
        }
        ClassLoader classLoader = BRLoadedApk.get(mBoundApplication.info).getClassLoader();
        Service service;
        try {
            service = (Service) classLoader.loadClass(serviceInfo.name).newInstance();
        } catch (ClassNotFoundException e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services class not found, skipping: " + serviceInfo.name);
                return null;
            }
            e.printStackTrace();
            Slog.e(TAG, "Unable to instantiate service " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, "Unable to instantiate service " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        }

        try {
            Context context = BlackBoxCore.getContext().createPackageContext(
                    serviceInfo.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            BRContextImpl.get(context).setOuterContext(service);
            BRService.get(service).attach(
                    context,
                    BlackBoxCore.mainThread(),
                    serviceInfo.name,
                    token,
                    mInitialApplication,
                    BRActivityManagerNative.get().getDefault()
            );
            ContextCompat.fix(context);
            service.onCreate();
            return service;
        } catch (Exception e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services service creation failed, skipping: " + serviceInfo.name);
                return null;
            }
            Slog.w(TAG, "Service creation failed, but continuing: " + serviceInfo.name + " - " + e.getMessage());
            return null;
        }
    }

    public JobService createJobService(ServiceInfo serviceInfo) {
        if (!BActivityThread.currentActivityThread().isInit()) {
            BActivityThread.currentActivityThread().bindApplication(serviceInfo.packageName, serviceInfo.processName);
        }
        ClassLoader classLoader = BRLoadedApk.get(mBoundApplication.info).getClassLoader();
        JobService service;
        try {
            service = (JobService) classLoader.loadClass(serviceInfo.name).newInstance();
        } catch (ClassNotFoundException e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services JobService class not found, skipping: " + serviceInfo.name);
                return null;
            }
            e.printStackTrace();
            Slog.e(TAG, "Unable to create JobService " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, "Unable to create JobService " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        }

        try {
            Context context = BlackBoxCore.getContext().createPackageContext(
                    serviceInfo.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            BRContextImpl.get(context).setOuterContext(service);
            BRService.get(service).attach(
                    context,
                    BlackBoxCore.mainThread(),
                    serviceInfo.name,
                    BActivityThread.currentActivityThread().getActivityThread(),
                    mInitialApplication,
                    BRActivityManagerNative.get().getDefault()
            );
            ContextCompat.fix(context);
            service.onCreate();
            service.onBind(null);
            return service;
        } catch (Exception e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services JobService creation failed, skipping: " + serviceInfo.name);
                return null;
            }
            Slog.w(TAG, "JobService creation failed, but continuing: " + serviceInfo.name + " - " + e.getMessage());
            return null;
        }
    }

    public void bindApplication(final String packageName, final String processName) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ConditionVariable conditionVariable = new ConditionVariable();
            BlackBoxCore.get().getHandler().post(() -> {
                
                Object bindData = createBindApplicationData(packageName, processName);
                handleBindApplication(packageName, processName);
                conditionVariable.open();
            });
            conditionVariable.block();
        } else {
            
            Object bindData = createBindApplicationData(packageName, processName);
            handleBindApplication(packageName, processName);
        }
    }
    
    
    private Object createBindApplicationData(String packageName, String processName) {
        try {
            
            PackageInfo packageInfo = BlackBoxCore.getBPackageManager().getPackageInfo(packageName, PackageManager.GET_PROVIDERS, getUserId());
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            
            
            
            return new Object() {
                public ApplicationInfo getInfo() { return applicationInfo; }
                public List<ProviderInfo> getProviders() { 
                    return packageInfo.providers != null ? Arrays.asList(packageInfo.providers) : new ArrayList<>();
                }
            };
        } catch (Exception e) {
            Slog.e(TAG, "Error creating bind application data", e);
            
            return new Object() {
                public ApplicationInfo getInfo() { return null; }
                public List<ProviderInfo> getProviders() { return new ArrayList<>(); }
            };
        }
    }

    public synchronized void handleBindApplication(String packageName, String processName) {
        if ( isInit())
            return;
        try {
            CrashHandler.create();
            // Boost main game thread priority for smoother frame pacing and lower input latency
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        } catch (Throwable ignored) {
        }

        PackageInfo packageInfo = BlackBoxCore.getBPackageManager().getPackageInfo(packageName, PackageManager.GET_PROVIDERS, BActivityThread.getUserId());
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo != null) {
            if (applicationInfo.metaData == null) {
                applicationInfo.metaData = new android.os.Bundle();
            }
            applicationInfo.metaData.putBoolean("IMSDK_WEBVIEW_USE_CUSTOM_ERROR_PAGE", false);
            applicationInfo.metaData.putString("IMSDK_WEBVIEW_USE_CUSTOM_ERROR_PAGE", "false");
        }
        if (packageInfo.providers == null) {
            packageInfo.providers = new ProviderInfo[]{};
        }
        mProviders.addAll(Arrays.asList(packageInfo.providers));

        Object boundApplication = BRActivityThread.get(BlackBoxCore.mainThread()).mBoundApplication();

        Context packageContext = createPackageContext(applicationInfo);
        Object loadedApk = BRContextImpl.get(packageContext).mPackageInfo();
        BRLoadedApk.get(loadedApk)._set_mSecurityViolation(false);
        
        BRLoadedApk.get(loadedApk)._set_mApplicationInfo(applicationInfo);
        fixLoadedApkDataDirs(loadedApk, packageContext, applicationInfo);

        int targetSdkVersion = applicationInfo.targetSdkVersion;
        if (targetSdkVersion < Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy newPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy()).permitNetwork().build();
            StrictMode.setThreadPolicy(newPolicy);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (targetSdkVersion < Build.VERSION_CODES.N) {
                StrictModeCompat.disableDeathOnFileUriExposure();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                String suffix = ("p_" + getUserId() + "_" + processName)
                        .replaceAll("[^a-zA-Z0-9_]", "_");
                WebView.setDataDirectorySuffix(suffix);
                if (packageContext != null && packageContext.getDataDir() != null) {
                    String[] dirs = new String[]{
                            "app_webview",
                            "app_webview_" + suffix,
                            "app_webview_" + suffix + "/Default",
                            "app_webview_" + suffix + "/Default/HTTP Cache",
                            "cache/webview",
                            "cache/webview_" + suffix,
                            "cache/webview_embedded",
                            "cache/webview_embedded/Helium Crashpad",
                            "cache/webview_embedded/Helium Crashpad/pending"
                    };
                    for (String d : dirs) {
                        File dir = new File(packageContext.getDataDir(), d);
                        if (!dir.exists()) dir.mkdirs();
                        dir.setReadable(true, false);
                        dir.setWritable(true, false);
                        dir.setExecutable(true, false);
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        VirtualRuntime.setupRuntime(processName, applicationInfo);

        BRVMRuntime.get(BRVMRuntime.get().getRuntime()).setTargetSdkVersion(applicationInfo.targetSdkVersion);
        if (BuildCompat.isS()) {
            BRCompatibility.get().setTargetSdkVersion(applicationInfo.targetSdkVersion);
        }

        NativeCore.init(Build.VERSION.SDK_INT);
        NativeCore.setPackageIdentity(BlackBoxCore.getHostPkg(), applicationInfo.packageName, processName);
        NativeCore.applyGuestSecurityAndDataRedirect();
        assert packageContext != null;
        IOCore.get().enableRedirect(packageContext);

        AppBindData bindData = new AppBindData();
        bindData.appInfo = applicationInfo;
        bindData.processName = processName;
        bindData.info = loadedApk;
        bindData.providers = mProviders;

        ActivityThreadAppBindDataContext activityThreadAppBindData = BRActivityThreadAppBindData.get(boundApplication);
        activityThreadAppBindData._set_instrumentationName(new ComponentName(bindData.appInfo.packageName, Instrumentation.class.getName()));
        activityThreadAppBindData._set_appInfo(bindData.appInfo);
        activityThreadAppBindData._set_info(bindData.info);
        activityThreadAppBindData._set_processName(bindData.processName);
        activityThreadAppBindData._set_providers(bindData.providers);

        mBoundApplication = bindData;

        
        if (BRNetworkSecurityConfigProvider.getRealClass() != null) {
            Security.removeProvider("AndroidNSSP");
            BRNetworkSecurityConfigProvider.get().install(packageContext);
        }
        Application application;
        try {
            onBeforeCreateApplication(packageName, processName, packageContext);
            
            
            try {
                application = BRLoadedApk.get(loadedApk).makeApplication(false, null);
            } catch (Exception makeAppException) {
                Slog.e(TAG, "Failed to makeApplication, trying fallback approach", makeAppException);
                application = null;
            }
            
            
            if (application == null) {
                Slog.w(TAG, "makeApplication returned null, attempting fallback creation");
                
                
                try {
                    application = BRLoadedApk.get(loadedApk).makeApplication(true, null);
                } catch (Exception e) {
                    Slog.e(TAG, "Fallback makeApplication also failed", e);
                }
                
                
                if (application == null) {
                    Slog.w(TAG, "Creating minimal application context as fallback");
                    try {
                        
                        application = (Application) packageContext;
                        if (application == null) {
                            Slog.e(TAG, "Even package context is null, this is critical");
                            throw new RuntimeException("Unable to create application context");
                        }
                    } catch (Exception contextException) {
                        Slog.e(TAG, "Failed to create fallback application context", contextException);
                        throw new RuntimeException("Unable to makeApplication - all fallback attempts failed", contextException);
                    }
                }
            }
            
            if (application == null) {
                Slog.e(TAG, "makeApplication application Error! All attempts failed");
                throw new RuntimeException("Unable to create application - all creation methods failed");
            }
            
            mInitialApplication = application;
            BRActivityThread.get(BlackBoxCore.mainThread())._set_mInitialApplication(mInitialApplication);
            try {
                mInitialApplication.registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, android.os.Bundle savedInstanceState) {}
                    @Override
                    public void onActivityStarted(Activity activity) {}
                    @Override
                    public void onActivityResumed(Activity activity) {
                    }
                    @Override
                    public void onActivityPaused(Activity activity) {}
                    @Override
                    public void onActivityStopped(Activity activity) {}
                    @Override
                    public void onActivitySaveInstanceState(Activity activity, android.os.Bundle outState) {}
                    @Override
                    public void onActivityDestroyed(Activity activity) {}
                });
                Slog.d(TAG, "Registered global ActivityLifecycleCallbacks to enforce WebView fixes");
            } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            ContextCompat.fix((Context) BRActivityThread.get(BlackBoxCore.mainThread()).getSystemContext());
            installProviders(mInitialApplication, bindData.processName, bindData.providers);

            onBeforeApplicationOnCreate(packageName, processName, application);
            AppInstrumentation.get().callApplicationOnCreate(application);
            onAfterApplicationOnCreate(packageName, processName, application);

            HookManager.get().checkEnv(HCallbackProxy.class);
        } catch (Exception e) {
            Slog.e(TAG, "Critical error in handleBindApplication", e);
            throw new RuntimeException("Unable to makeApplication", e);
        }
    }
    
    
    private void initializeJarEnvironment() {
        try {
            Slog.d(TAG, "Initializing JAR environment for DEX loading");
            
            
            JarManager jarManager = JarManager.getInstance();
            if (!jarManager.isReady()) {
                Slog.d(TAG, "JarManager not ready, initializing synchronously");
                jarManager.initializeSync();
            }
            
            
            File emptyJar = jarManager.getEmptyJar();
            if (emptyJar == null || !emptyJar.exists()) {
                Slog.w(TAG, "Empty JAR not available, attempting to recreate");
                jarManager.clearCache();
                jarManager.initializeSync();
                emptyJar = jarManager.getEmptyJar();
            }
            
            if (emptyJar != null && emptyJar.exists()) {
                Slog.d(TAG, "Empty JAR verified: " + emptyJar.getAbsolutePath());
            } else {
                Slog.w(TAG, "Empty JAR still not available after retry");
            }
            
        } catch (Exception e) {
            Slog.e(TAG, "Error initializing JAR environment", e);
        }
    }
    
    
    private Application createApplicationWithFallback(android.content.pm.ApplicationInfo appInfo) {
        try {
            
            Application application = createApplication(appInfo);
            if (application != null) {
                Slog.d(TAG, "Application created successfully: " + appInfo.className);
                return application;
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to create application normally: " + e.getMessage());
        }
        
        try {
            
            Slog.d(TAG, "Attempting fallback application creation");
            ClassLoader classLoader = getClassLoader(appInfo);
            if (classLoader == null) {
                Slog.w(TAG, "ClassLoader is null, using system class loader");
                classLoader = ClassLoader.getSystemClassLoader();
            }
            
            Class<?> appClass = classLoader.loadClass(appInfo.className);
            Application application = (Application) appClass.newInstance();
            
            
            ensureApplicationBaseContext(application, appInfo);
            
            Slog.d(TAG, "Fallback application creation successful");
            return application;
            
        } catch (Exception e) {
            Slog.e(TAG, "Fallback application creation failed: " + e.getMessage());
            
            
            try {
                Slog.d(TAG, "Creating minimal application wrapper");
                Application wrapper = new Application() {
                    @Override
                    public void onCreate() {
                        super.onCreate();
                        Slog.d(TAG, "Minimal application wrapper onCreate called");
                    }
                };
                
                
                ensureApplicationBaseContext(wrapper, appInfo);
                
                return wrapper;
            } catch (Exception wrapperException) {
                Slog.e(TAG, "Failed to create minimal application wrapper", wrapperException);
                return null;
            }
        }
    }
    
    
    private void installContentProvidersWithFallback(Application application, Object data) {
        try {
            List<android.content.pm.ProviderInfo> providers = getProviderInfoList(data);
            if (providers == null || providers.isEmpty()) {
                Slog.d(TAG, "No content providers to install");
                return;
            }
            
            Slog.d(TAG, "Installing " + providers.size() + " content providers");
            
            for (android.content.pm.ProviderInfo providerInfo : providers) {
                try {
                    installContentProvider(application, providerInfo);
                    Slog.d(TAG, "Successfully installed provider: " + providerInfo.name);
                } catch (Exception e) {
                    Slog.w(TAG, "Failed to install provider " + providerInfo.name + ": " + e.getMessage());
                    
                }
            }
            
        } catch (Exception e) {
            Slog.e(TAG, "Error installing content providers", e);
        }
    }
    
    
    private android.content.pm.ApplicationInfo getApplicationInfo(Object data) {
        try {
            
            if (data != null) {
                try {
                    
                    Method getInfoMethod = data.getClass().getMethod("getInfo");
                    ApplicationInfo appInfo = (ApplicationInfo) getInfoMethod.invoke(data);
                    if (appInfo != null) {
                        return appInfo;
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Error getting info from custom data object: " + e.getMessage());
                }
            }
            
            
            String packageName = BlackBoxCore.getAppPackageName();
            if (packageName != null) {
                PackageInfo packageInfo = BlackBoxCore.getBPackageManager().getPackageInfo(packageName, 0, getUserId());
                return packageInfo.applicationInfo;
            }
            
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "Error getting application info", e);
            return null;
        }
    }
    
    
    private ClassLoader getClassLoader(android.content.pm.ApplicationInfo appInfo) {
        try {
            
            String sourceDir = appInfo.sourceDir;
            if (sourceDir != null) {
                return new dalvik.system.PathClassLoader(sourceDir, ClassLoader.getSystemClassLoader());
            }
            
            
            return ClassLoader.getSystemClassLoader();
        } catch (Exception e) {
            Slog.w(TAG, "Error getting class loader: " + e.getMessage());
            return ClassLoader.getSystemClassLoader();
        }
    }

    
    private Application createApplication(android.content.pm.ApplicationInfo appInfo) {
        try {
            
            ClassLoader classLoader = getClassLoader(appInfo);
            Class<?> appClass = classLoader.loadClass(appInfo.className);
            Application application = (Application) appClass.newInstance();
            
            
            ensureApplicationBaseContext(application, appInfo);
            
            return application;
        } catch (Exception e) {
            Slog.e(TAG, "Error creating application: " + e.getMessage());
            return null;
        }
    }
    
    
    private void ensureApplicationBaseContext(Application application, android.content.pm.ApplicationInfo appInfo) {
        try {
            
            if (application.getBaseContext() != null) {
                Slog.d(TAG, "Application already has base context: " + appInfo.className);
                return;
            }
            
            
            Context packageContext = createPackageContext(appInfo);
            if (packageContext == null) {
                Slog.w(TAG, "Could not create package context for application: " + appInfo.className + ", using fallback");
                
                packageContext = createFallbackContext(appInfo.packageName);
            }
            
            
            if (packageContext == null) {
                Slog.e(TAG, "Failed to create any context for application: " + appInfo.className);
                return;
            }
            
            
            try {
                Method attachBaseContext = Application.class.getDeclaredMethod("attachBaseContext", Context.class);
                attachBaseContext.setAccessible(true);
                attachBaseContext.invoke(application, packageContext);
                Slog.d(TAG, "Successfully attached base context to application: " + appInfo.className);
            } catch (Exception e) {
                Slog.w(TAG, "Could not attach base context to application: " + e.getMessage());
            }
            
        } catch (Exception e) {
            Slog.e(TAG, "Error ensuring application base context: " + e.getMessage());
        }
    }
    
    
    private Context createFallbackContext(String packageName) {
        try {
            Context baseContext = BlackBoxCore.getContext();
            if (baseContext == null) {
                Slog.e(TAG, "BlackBoxCore.getContext() is null, cannot create fallback context");
                return null;
            }
            
            
            return new ContextWrapper(baseContext) {
                @Override
                public String getPackageName() {
                    return packageName;
                }
                
                @Override
                public android.content.pm.PackageManager getPackageManager() {
                    try {
                        return baseContext.getPackageManager();
                    } catch (Exception e) {
                        Slog.w(TAG, "Error getting package manager from base context: " + e.getMessage());
                        return null;
                    }
                }
                
                @Override
                public android.content.res.Resources getResources() {
                    try {
                        return baseContext.getResources();
                    } catch (Exception e) {
                        Slog.w(TAG, "Error getting resources from base context: " + e.getMessage());
                        try {
                            return android.content.res.Resources.getSystem();
                        } catch (Exception e2) {
                            Slog.e(TAG, "Error getting system resources: " + e2.getMessage());
                            return null;
                        }
                    }
                }
                
                @Override
                public ClassLoader getClassLoader() {
                    try {
                        return baseContext.getClassLoader();
                    } catch (Exception e) {
                        Slog.w(TAG, "Error getting class loader from base context: " + e.getMessage());
                        try {
                            return ClassLoader.getSystemClassLoader();
                        } catch (Exception e2) {
                            Slog.e(TAG, "Error getting system class loader: " + e2.getMessage());
                            return null;
                        }
                    }
                }
                
                @Override
                public Context getApplicationContext() {
                    try {
                        return baseContext.getApplicationContext();
                    } catch (Exception e) {
                        Slog.w(TAG, "Error getting application context from base context: " + e.getMessage());
                        return this;
                    }
                }
            };
        } catch (Exception e) {
            Slog.e(TAG, "Failed to create fallback context for " + packageName + ": " + e.getMessage());
            return null;
        }
    }
    
    
    private List<android.content.pm.ProviderInfo> getProviderInfoList(Object data) {
        try {
            
            if (data != null) {
                try {
                    
                    Method getProvidersMethod = data.getClass().getMethod("getProviders");
                    List<ProviderInfo> providers = (List<ProviderInfo>) getProvidersMethod.invoke(data);
                    if (providers != null) {
                        return providers;
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Error getting providers from custom data object: " + e.getMessage());
                }
            }
            
            
            return new ArrayList<>();
        } catch (Exception e) {
            Slog.e(TAG, "Error getting provider info list", e);
            return new ArrayList<>();
        }
    }
    
    
    private void installContentProvider(Application application, android.content.pm.ProviderInfo providerInfo) {
        try {

            if (application == null) {
                Slog.w(TAG, "Application is null, cannot install content provider: " + providerInfo.name);
                return;
            }

            // Skip known anti-virtual-environment detection providers.
            // These providers detect the sandbox and call System.exit() to kill the app.
            // By skipping them, the app continues to run normally.
            if (providerInfo.name != null && isAntiDetectProvider(providerInfo.name)) {
                Slog.w(TAG, "Skipping anti-detect ContentProvider: " + providerInfo.name);
                return;
            }

            ClassLoader classLoader = application.getClassLoader();
            if (classLoader == null) {
                Slog.w(TAG, "Application class loader is null, using system class loader for: " + providerInfo.name);
                classLoader = ClassLoader.getSystemClassLoader();
            }

            android.content.ContentProvider provider = (android.content.ContentProvider) classLoader
                .loadClass(providerInfo.name).newInstance();

            provider.attachInfo(application, providerInfo);

            Slog.d(TAG, "Content provider installed: " + providerInfo.name);

        } catch (Exception e) {
            Slog.e(TAG, "Error installing content provider " + providerInfo.name, e);
        }
    }

    /**
     * Check if a ContentProvider is a known anti-virtual-environment detector.
     * These providers detect sandboxed environments and force-kill the app.
     */
    private static boolean isAntiDetectProvider(String className) {
        String lower = className.toLowerCase();
        // Meituan Hades anti-cheat
        return lower.contains("hades")
                || lower.contains("ztuni")
                // Douyin/TikTok security
                || lower.contains("securityguard")
                || lower.contains("avdetector")
                // Common detection patterns
                || lower.contains("virtualdetect")
                || lower.contains("sandboxdetect")
                || lower.contains("emulatordetect")
                || lower.contains("fridadetect")
                || lower.contains("hookdetect")
                || lower.contains("xposeddetect")
                || lower.contains("magiskdetect");
    }
    
    
    private void setApplication(Application application) {
        try {
            mInitialApplication = application;
            BRActivityThread.get(BlackBoxCore.mainThread())._set_mInitialApplication(application);
            Slog.d(TAG, "Application set in ActivityThread successfully");
        } catch (Exception e) {
            Slog.e(TAG, "Error setting application in ActivityThread", e);
        }
    }

    private void handleSecurityException(SecurityException se, String packageName, String processName, Context packageContext) {
        Slog.w(TAG, "Handling SecurityException for " + packageName);
        
        
        try {
                            Application basicApp = createMinimalApplication(packageContext, packageName);
            if (basicApp != null) {
                mInitialApplication = basicApp;
                BRActivityThread.get(BlackBoxCore.mainThread())._set_mInitialApplication(mInitialApplication);
                ContextCompat.fix(mInitialApplication);
                
                
                Slog.w(TAG, "Created basic application, skipping problematic operations");
                return;
            }
        } catch (Exception e) {
            Slog.e(TAG, "Failed to create basic application after SecurityException: " + e.getMessage());
        }
        
        
        throw new RuntimeException("Unable to handle SecurityException", se);
    }

    private void installProvidersWithErrorHandling(Context context, String processName, List<ProviderInfo> providers) {
        long origId = Binder.clearCallingIdentity();
        try {
            for (ProviderInfo providerInfo : providers) {
                try {
                    if (providerInfo.name != null && isAntiDetectProvider(providerInfo.name)) {
                        continue;
                    }
                    boolean shouldInstall = providerInfo.multiprocess
                            || processName.equals(providerInfo.processName)
                            || (processName.equals(context.getPackageName()) && (providerInfo.processName == null || providerInfo.processName.equals(context.getPackageName())));
                    if (shouldInstall) {
                        installProvider(BlackBoxCore.mainThread(), context, providerInfo, null);
                    }
                } catch (SecurityException se) {
                    Slog.w(TAG, "SecurityException installing provider " + providerInfo.name + ": " + se.getMessage());
                    
                } catch (Throwable t) {
                    Slog.w(TAG, "Error installing provider " + providerInfo.name + ": " + t.getMessage());
                    
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
            try {
                ContentProviderDelegate.init();
            } catch (Exception e) {
                Slog.w(TAG, "Error initializing ContentProviderDelegate: " + e.getMessage());
            }
        }
    }

    public static Context createPackageContext(ApplicationInfo info) {
        try {
            return BlackBoxCore.getContext().createPackageContext(info.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    private static Context createMinimalPackageContext(ApplicationInfo info) {
        try {
            
            Context baseContext = BlackBoxCore.getContext();
            
            
            try {
                Context packageContext = baseContext.createPackageContext(info.packageName, 0);
                if (packageContext != null) {
                    Slog.d(TAG, "Successfully created package context with minimal flags for " + info.packageName);
                    return packageContext;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Failed to create package context with minimal flags for " + info.packageName + ": " + e.getMessage());
            }
            
            
            try {
                Context packageContext = baseContext.createPackageContext(info.packageName, Context.CONTEXT_IGNORE_SECURITY);
                if (packageContext != null) {
                    Slog.d(TAG, "Successfully created package context with ignore security for " + info.packageName);
                    return packageContext;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Failed to create package context with ignore security for " + info.packageName + ": " + e.getMessage());
            }
            
            
            try {
                Context packageContext = baseContext.createPackageContext(info.packageName, Context.CONTEXT_INCLUDE_CODE);
                if (packageContext != null) {
                    Slog.d(TAG, "Successfully created package context with include code for " + info.packageName);
                    return packageContext;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Failed to create package context with include code for " + info.packageName + ": " + e.getMessage());
            }
            
        } catch (Exception e) {
            Slog.e(TAG, "Failed to create minimal package context for " + info.packageName + ": " + e.getMessage());
        }
        
        
        Slog.w(TAG, "Using base context as fallback for " + info.packageName);
        return createWrappedBaseContext(info.packageName);
    }

    
    private static Context createWrappedBaseContext(String packageName) {
        try {
            Context baseContext = BlackBoxCore.getContext();
            
            
            return new ContextWrapper(baseContext) {
                @Override
                public String getPackageName() {
                    return packageName;
                }
                
                @Override
                public PackageManager getPackageManager() {
                    return baseContext.getPackageManager();
                }
                
                @Override
                public Resources getResources() {
                    return baseContext.getResources();
                }
                
                @Override
                public ClassLoader getClassLoader() {
                    return baseContext.getClassLoader();
                }
                
                @Override
                public Context getApplicationContext() {
                    return baseContext.getApplicationContext();
                }
            };
        } catch (Exception e) {
            Slog.e(TAG, "Failed to create wrapped base context for " + packageName + ": " + e.getMessage());
            
            return BlackBoxCore.getContext();
        }
    }

    private void installProviders(Context context, String processName, List<ProviderInfo> provider) {
        long origId = Binder.clearCallingIdentity();
        try {
            for (ProviderInfo providerInfo : provider) {
                try {
                    // Skip known anti-virtual-environment detection providers.
                    if (providerInfo.name != null && isAntiDetectProvider(providerInfo.name)) {
                        Slog.w(TAG, "Skipping anti-detect ContentProvider: " + providerInfo.name);
                        continue;
                    }
                    boolean shouldInstall = providerInfo.multiprocess
                            || processName.equals(providerInfo.processName)
                            || (processName.equals(context.getPackageName()) && (providerInfo.processName == null || providerInfo.processName.equals(context.getPackageName())));
                    if (shouldInstall) {
                        installProvider(BlackBoxCore.mainThread(), context, providerInfo, null);
                    }
                } catch (Throwable ignored) {
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
            ContentProviderDelegate.init();
        }
    }

    public Object getPackageInfo() {
        return mBoundApplication.info;
    }

    public static void installProvider(Object mainThread, Context context, ProviderInfo providerInfo, Object holder) throws Throwable {
        Method installProvider = Reflector.findMethodByFirstName(mainThread.getClass(), "installProvider");
        if (installProvider != null) {
            installProvider.setAccessible(true);
            installProvider.invoke(mainThread, context, holder, providerInfo, false, true, true);
        }
    }



    @Override
    public IBinder getActivityThread() {
        return BRActivityThread.get(BlackBoxCore.mainThread()).getApplicationThread();
    }

    @Override
    public void bindApplication() {
        if (!isInit()) {
            bindApplication(getAppPackageName(), getAppProcessName());
        }
    }

    @Override
    public void stopService(Intent intent) {
        AppServiceDispatcher.get().stopService(intent);
    }

    @Override
    public void restartJobService(String selfId) throws RemoteException {

    }

    @Override
    public IBinder acquireContentProviderClient(ProviderInfo providerInfo) throws RemoteException {
        if (!isInit()) {
            bindApplication(BActivityThread.getAppConfig().packageName, BActivityThread.getAppConfig().processName);
        }
        String[] split = providerInfo.authority.split(";");
        for (String auth : split) {
            ContentProviderClient contentProviderClient = BlackBoxCore.getContext()
                    .getContentResolver().acquireContentProviderClient(auth);
            if (contentProviderClient == null) {
                continue;
            }
            try {
                IInterface iInterface = BRContentProviderClient.get(contentProviderClient).mContentProvider();
                if (iInterface != null) {
                    return iInterface.asBinder();
                }
            } finally {
                contentProviderClient.release();
            }
        }
        return null;
    }

    @Override
    public IBinder peekService(Intent intent) {
        return AppServiceDispatcher.get().peekService(intent);
    }

    @Override
    public void finishActivity(final IBinder token) {
        mH.post(() -> {
            Map<IBinder, Object> activities = BRActivityThread.get(BlackBoxCore.mainThread()).mActivities();
            if (activities.isEmpty())
                return;
            Object clientRecord = activities.get(token);
            if (clientRecord == null)
                return;
            Activity activity = getActivityByToken(token);

            while (activity.getParent() != null) {
                activity = activity.getParent();
            }

            int resultCode = BRActivity.get(activity).mResultCode();
            Intent resultData = BRActivity.get(activity).mResultData();
            ActivityManagerCompat.finishActivity(token, resultCode, resultData);
            BRActivity.get(activity)._set_mFinished(true);
        });
    }

    @Override
    public void handleNewIntent(final IBinder token, final Intent intent) {
        mH.post(() -> {
            Intent newIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                newIntent = BRReferrerIntent.get()._new(intent, BlackBoxCore.getHostPkg());
            } else {
                newIntent = intent;
            }
            Object mainThread = BlackBoxCore.mainThread();
            if (BRActivityThread.get(BlackBoxCore.mainThread())._check_performNewIntents(null, null) != null) {
                BRActivityThread.get(mainThread).performNewIntents(
                        token,
                        Collections.singletonList(newIntent)
                );
            } else if (BRActivityThreadNMR1.get(mainThread)._check_performNewIntents(null, null, false) != null) {
                BRActivityThreadNMR1.get(mainThread).performNewIntents(
                        token,
                        Collections.singletonList(newIntent),
                        true);
            } else if (BRActivityThreadQ.get(mainThread)._check_handleNewIntent(null, null) != null) {
                BRActivityThreadQ.get(mainThread).handleNewIntent(token, Collections.singletonList(newIntent));
            }
        });
    }

    @Override
    public void scheduleReceiver(ReceiverData data) throws RemoteException {
        if (!isInit()) {
            bindApplication();
        }
        mH.post(() -> {
            BroadcastReceiver mReceiver = null;
            Intent intent = data.intent;
            ActivityInfo activityInfo = data.activityInfo;
            BroadcastReceiver.PendingResult pendingResult = data.data.build();

            try {
                Context baseContext = mInitialApplication.getBaseContext();
                ClassLoader classLoader = baseContext.getClassLoader();
                intent.setExtrasClassLoader(classLoader);

                mReceiver = (BroadcastReceiver) classLoader.loadClass(activityInfo.name).newInstance();
                BRBroadcastReceiver.get(mReceiver).setPendingResult(pendingResult);
                mReceiver.onReceive(baseContext, intent);
                BroadcastReceiver.PendingResult finish = BRBroadcastReceiver.get(mReceiver).getPendingResult();
                if (finish != null) {
                    finish.finish();
                }
                BlackBoxCore.getBActivityManager().finishBroadcast(data.data);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                Slog.e(TAG,
                        "Error receiving broadcast " + intent
                                + " in " + mReceiver);
            }
        });
    }

    public static Activity getActivityByToken(IBinder token) {
        Map<IBinder, Object> iBinderObjectMap =
                BRActivityThread.get(BlackBoxCore.mainThread()).mActivities();
        return BRActivityThreadActivityClientRecord.get(iBinderObjectMap.get(token)).activity();
    }

    private void onBeforeCreateApplication(String packageName, String processName, Context context) {
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.beforeCreateApplication(packageName, processName, context, BActivityThread.getUserId());
        }
    }

    private void onBeforeApplicationOnCreate(String packageName, String processName, Application application) {
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.beforeApplicationOnCreate(packageName, processName, application, BActivityThread.getUserId());
        }
    }

    private void onAfterApplicationOnCreate(String packageName, String processName, Application application) {
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.afterApplicationOnCreate(packageName, processName, application, BActivityThread.getUserId());
        }
        try {
            boolean isGameCandidate = !packageName.startsWith("com.google.") && !packageName.equals("com.android.vending");
            if (isGameCandidate) {
                // Pass 1: Inject downloaded/custom native mod .so libraries right after Application.onCreate
                NativeCore.injectGameModLibraries(packageName);

                // Pass 2: Start a watcher thread only for games
                new Thread(() -> {
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < 15000) {
                        try {
                            Thread.sleep(1500);
                            if (NativeCore.injectGameModLibraries(packageName) > 0) {
                                break;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }, "ZynoxModWatcher").start();
            }

            // Pass 3: Also hook Activity creation so any .so that waits for GameActivity / libUE4.so is guaranteed to load,
            // and configure any in-game OAuth WebView (e.g. WebLoginActivity) with LOAD_NO_CACHE & third-party cookies.
            if (application != null) {
                application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, android.os.Bundle savedInstanceState) {
                        try {
                            NativeCore.injectGameModLibraries(packageName);
                        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                        try {
                            configureActivityWebViews(activity);
                        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                    }
                    @Override
                    public void onActivityStarted(Activity activity) {
                        try {
                            configureActivityWebViews(activity);
                        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                    }
                    @Override
                    public void onActivityResumed(Activity activity) {
                        try {
                            NativeCore.injectGameModLibraries(packageName);
                        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                        try {
                            configureActivityWebViews(activity);
                        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                    }
                    @Override public void onActivityPaused(Activity activity) {}
                    @Override public void onActivityStopped(Activity activity) {}
                    @Override public void onActivitySaveInstanceState(Activity activity, android.os.Bundle outState) {}
                    @Override public void onActivityDestroyed(Activity activity) {}
                });
            }
        } catch (Throwable t) {
            Slog.w(TAG, "Native mod injection hook note: " + t.getMessage());
        }
    }

    private static void fixLoadedApkDataDirs(Object loadedApk, Context packageContext, ApplicationInfo applicationInfo) {
        if (loadedApk == null || applicationInfo == null || applicationInfo.dataDir == null) return;
        try {
            File dataDirFile = new File(applicationInfo.dataDir);
            if (!dataDirFile.exists()) dataDirFile.mkdirs();
            String deDir = applicationInfo.deviceProtectedDataDir != null
                    ? applicationInfo.deviceProtectedDataDir
                    : applicationInfo.dataDir;
            File deDirFile = new File(deDir);
            if (!deDirFile.exists()) deDirFile.mkdirs();

            Class<?> loadedApkClass = loadedApk.getClass();
            setFieldQuietly(loadedApkClass, loadedApk, "mDataDir", applicationInfo.dataDir);
            setFieldQuietly(loadedApkClass, loadedApk, "mDataDirFile", dataDirFile);
            setFieldQuietly(loadedApkClass, loadedApk, "mDeviceProtectedDataDirFile", deDirFile);
            setFieldQuietly(loadedApkClass, loadedApk, "mCredentialProtectedDataDirFile", dataDirFile);

            if (packageContext != null) {
                Class<?> ctxClass = packageContext.getClass();
                setFieldQuietly(ctxClass, packageContext, "mFilesDir", null);
                setFieldQuietly(ctxClass, packageContext, "mCacheDir", null);
                setFieldQuietly(ctxClass, packageContext, "mCodeCacheDir", null);
                setFieldQuietly(ctxClass, packageContext, "mNoBackupFilesDir", null);
                try { packageContext.getFilesDir(); } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                try { packageContext.getCacheDir(); } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                try { packageContext.getCodeCacheDir(); } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }
        } catch (Throwable t) {
            Slog.w(TAG, "fixLoadedApkDataDirs note: " + t.getMessage());
        }
    }

    private static void setFieldQuietly(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            java.lang.reflect.Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
    }

    private static final java.util.Set<Integer> sConfiguredWebViews =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private static final java.util.Map<String, Long> sLastOAuthRetryTime =
            java.util.Collections.synchronizedMap(new java.util.HashMap<>());

    public static void configureActivityWebViews(final Activity activity) {
        if (activity == null) return;
        String pkg = activity.getPackageName();
        if (pkg != null && (pkg.equals("com.android.vending")
                || pkg.equals("com.google.android.gms")
                || pkg.equals("com.google.android.gsf")
                || pkg.equals("com.google.android.play.games")
                || pkg.equals(BlackBoxCore.getHostPkg()))) {
            return;
        }

        String className = activity.getClass().getName();
        if (!className.contains("Web") && !className.contains("OAuth") && !className.contains("Login") && !className.contains("Browser")) {
            return;
        }

        try {
            final String fallbackUrl = extractOAuthUrlFromActivity(activity);
            scanActivityFieldsForWebViews(activity, fallbackUrl);

            android.view.Window window = activity.getWindow();
            if (window == null) return;
            final android.view.View decor = window.getDecorView();
            if (decor == null) return;

            scanAndFixWebViews(decor, fallbackUrl);

            // Active monitor while activity is open: check periodically to wrap client and fix UI
            final Handler h = BlackBoxCore.get().getHandler();
            h.post(new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    if (activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
                        return;
                    }
                    try {
                        String url = extractOAuthUrlFromActivity(activity);
                        scanActivityFieldsForWebViews(activity, url);
                        scanAndFixWebViews(decor, url);

                        // Reset Tencent WebLoginActivity error title if it displayed "Webpage not available"
                        int titleId = activity.getResources().getIdentifier("web_login_tv_title", "id", activity.getPackageName());
                        if (titleId != 0) {
                            android.view.View tv = activity.findViewById(titleId);
                            if (tv instanceof android.widget.TextView) {
                                CharSequence text = ((android.widget.TextView) tv).getText();
                                if (text != null && text.toString().toLowerCase().contains("not available")) {
                                    ((android.widget.TextView) tv).setText("Discord Login");
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                    count++;
                    if (count < 5) {
                        h.postDelayed(this, 500);
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static void scanActivityFieldsForWebViews(Activity activity, String fallbackUrl) {
        if (activity == null) return;
        try {
            Class<?> c = activity.getClass();
            while (c != null && c != Activity.class && c != Object.class) {
                java.lang.reflect.Field[] fields = c.getDeclaredFields();
                for (java.lang.reflect.Field f : fields) {
                    f.setAccessible(true);
                    Object val = f.get(activity);
                    if (val instanceof WebView) {
                        applyWebViewFixes((WebView) val, fallbackUrl);
                    } else if (val instanceof android.view.View) {
                        scanAndFixWebViews((android.view.View) val, fallbackUrl);
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Throwable ignored) {
        }
    }

    private static String extractHttpUrlFromString(String s) {
        if (s == null || s.isEmpty()) return null;
        if (s.startsWith("https://") || s.startsWith("http://")) {
            return s;
        }
        int idx = s.indexOf("https://");
        if (idx < 0) idx = s.indexOf("http://");
        if (idx >= 0) {
            int end = idx;
            while (end < s.length()) {
                char ch = s.charAt(end);
                if (ch == '"' || ch == '\'' || ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '>' || ch == '}') {
                    break;
                }
                end++;
            }
            if (end > idx + 8) {
                return s.substring(idx, end);
            }
        }
        return null;
    }

    private static String extractOAuthUrlFromActivity(Activity activity) {
        if (activity == null) return null;
        try {
            // First check if the activity has buildUrl() method (Tencent WebLoginActivity)
            try {
                java.lang.reflect.Method buildUrlMethod = activity.getClass().getDeclaredMethod("buildUrl");
                buildUrlMethod.setAccessible(true);
                Object res = buildUrlMethod.invoke(activity);
                if (res instanceof String) {
                    String u = extractHttpUrlFromString((String) res);
                    if (u != null && !u.isEmpty()) {
                        Slog.i(TAG, "extractOAuthUrlFromActivity found via buildUrl(): " + u);
                        return u;
                    }
                }
            } catch (Throwable ignored) {
            }

            Intent intent = activity.getIntent();
            if (intent != null) {
                try {
                    intent.setExtrasClassLoader(activity.getClassLoader());
                } catch (Throwable ignored) {
                }
                String dataStr = extractHttpUrlFromString(intent.getDataString());
                if (dataStr != null) {
                    return dataStr;
                }
                String fromBundle = extractUrlFromBundle(intent.getExtras(), 0, activity.getClassLoader());
                if (fromBundle != null) return fromBundle;
            }
            Class<?> c = activity.getClass();
            while (c != null && c != Activity.class && c != Object.class) {
                java.lang.reflect.Field[] fields = c.getDeclaredFields();
                for (java.lang.reflect.Field f : fields) {
                    f.setAccessible(true);
                    Object val = f.get(activity);
                    if (val instanceof String) {
                        String u = extractHttpUrlFromString((String) val);
                        if (u != null) return u;
                    } else if (val != null && !val.getClass().isPrimitive() && !val.getClass().getName().startsWith("android.")) {
                        try {
                            for (java.lang.reflect.Field nf : val.getClass().getDeclaredFields()) {
                                if (nf.getType() == String.class) {
                                    nf.setAccessible(true);
                                    Object nval = nf.get(val);
                                    if (nval instanceof String) {
                                        String u = extractHttpUrlFromString((String) nval);
                                        if (u != null) return u;
                                    }
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static String extractUrlFromBundle(android.os.Bundle bundle, int depth, ClassLoader cl) {
        if (bundle == null || depth > 3) return null;
        try {
            if (cl != null) {
                bundle.setClassLoader(cl);
            }
            for (String key : bundle.keySet()) {
                Object val = bundle.get(key);
                if (val instanceof String) {
                    String u = extractHttpUrlFromString((String) val);
                    if (u != null) return u;
                } else if (val instanceof android.net.Uri) {
                    String u = extractHttpUrlFromString(val.toString());
                    if (u != null) return u;
                } else if (val instanceof android.os.Bundle) {
                    String nested = extractUrlFromBundle((android.os.Bundle) val, depth + 1, cl);
                    if (nested != null) return nested;
                } else if (val != null) {
                    try {
                        java.lang.reflect.Field[] fields = val.getClass().getDeclaredFields();
                        for (java.lang.reflect.Field f : fields) {
                            if (f.getType() == String.class) {
                                f.setAccessible(true);
                                Object fv = f.get(val);
                                if (fv instanceof String) {
                                    String u = extractHttpUrlFromString((String) fv);
                                    if (u != null) return u;
                                }
                            }
                        }
                    } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                }
            }
        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
        return null;
    }

    private static void scanAndFixWebViews(android.view.View root, String fallbackUrl) {
        if (root == null) return;
        if (root instanceof WebView) {
            applyWebViewFixes((WebView) root, fallbackUrl);
        } else if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) root;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                scanAndFixWebViews(vg.getChildAt(i), fallbackUrl);
            }
        }
    }

    public static class BlackBoxWrappedClient extends android.webkit.WebViewClient {
        public final android.webkit.WebViewClient origClient;

        public BlackBoxWrappedClient(android.webkit.WebViewClient origClient) {
            this.origClient = origClient;
        }

        private boolean handleCustomUrlLoading(WebView view, String url) {
            if (url == null) return false;
            if (url.contains("itop_webview_error.html") || url.contains("errorCode=")) {
                Slog.w(TAG, "Blocked overrideUrlLoading for error page: " + url);
                return true;
            }

            String lower = url.toLowerCase();
            boolean isHttp = lower.startsWith("http://") || lower.startsWith("https://")
                    || lower.startsWith("javascript:") || lower.startsWith("about:")
                    || lower.startsWith("file:") || lower.startsWith("data:");

            if (!isHttp) {
                try {
                    Intent intent;
                    if (lower.startsWith("intent://")) {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    } else {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    }
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        ResolveInfo vResolve = BlackBoxCore.getBPackageManager().resolveActivity(intent, 0, null, BActivityThread.getUserId());
                        if (vResolve != null && vResolve.activityInfo != null) {
                            intent.setComponent(new ComponentName(vResolve.activityInfo.packageName, vResolve.activityInfo.name));
                            Slog.i(TAG, "Routing custom scheme to virtual activity: " + vResolve.activityInfo.name + " (" + url + ")");
                            BlackBoxCore.getBActivityManager().startActivity(intent, BActivityThread.getUserId());
                            return true;
                        }

                        try {
                            Slog.i(TAG, "Launching custom scheme on host: " + url);
                            view.getContext().startActivity(intent);
                        } catch (Throwable t) {
                            Slog.w(TAG, "Host launch for custom scheme failed: " + t.getMessage());
                        }
                        return true;
                    }
                } catch (Throwable t) {
                    Slog.e(TAG, "Error routing custom scheme URL: " + url, t);
                    return true;
                }
            }

            if (origClient != null) {
                try {
                    return origClient.shouldOverrideUrlLoading(view, url);
                } catch (Throwable t) {
                    Slog.w(TAG, "origClient.shouldOverrideUrlLoading failed: " + t.getMessage());
                }
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleCustomUrlLoading(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
            if (request != null && request.getUrl() != null) {
                return handleCustomUrlLoading(view, request.getUrl().toString());
            }
            if (origClient != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && request != null) {
                try {
                    return origClient.shouldOverrideUrlLoading(view, request);
                } catch (Throwable t) {
                    Slog.w(TAG, "origClient.shouldOverrideUrlLoading(request) failed: " + t.getMessage());
                }
            }
            return false;
        }

        @Override
        public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
            if (request != null && request.getUrl() != null) {
                android.webkit.WebResourceResponse resp = handleInterceptRequest(request.getUrl().toString(), request.getMethod(), request.getRequestHeaders());
                if (resp != null) {
                    return resp;
                }
            }
            if (origClient != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    return origClient.shouldInterceptRequest(view, request);
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            android.webkit.WebResourceResponse resp = handleInterceptRequest(url, "GET", null);
            if (resp != null) {
                return resp;
            }
            if (origClient != null) {
                try {
                    return origClient.shouldInterceptRequest(view, url);
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            if (url != null && (url.contains("access_token=") || url.contains("code=") || url.contains("login_success") 
                    || url.contains("itop_result=") || url.contains("oauth2") || url.contains("accounts.google.com"))) {
                try {
                    android.webkit.CookieManager.getInstance().flush();
                } catch (Throwable ignored) {}
            }
            if (origClient != null) {
                try {
                    origClient.onPageStarted(view, url, favicon);
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Slog.i(TAG, "WebView onPageFinished: " + url + " | title=" + (view != null ? view.getTitle() : "null"));
            try {
                android.webkit.CookieManager.getInstance().flush();
            } catch (Throwable ignored) {}
            if (origClient != null) {
                try {
                    origClient.onPageFinished(view, url);
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }
        }

        @Override
        public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
            Slog.w(TAG, "WebView onReceivedSslError proceeding: " + error);
            if (handler != null) {
                handler.proceed();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Slog.w(TAG, "WebView onReceivedError (legacy): code=" + errorCode + ", desc=" + description + ", url=" + failingUrl);
            if (description != null && description.contains("ERR_CACHE_MISS") && failingUrl != null) {
                Slog.w(TAG, "Recovering from ERR_CACHE_MISS (legacy) by explicitly loading URL");
                view.post(() -> {
                    view.loadUrl(failingUrl);
                });
            }
        }

        @Override
        public void onReceivedError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request != null && error != null) {
                int code = error.getErrorCode();
                String desc = error.getDescription() != null ? error.getDescription().toString() : "";
                String fUrl = request.getUrl() != null ? request.getUrl().toString() : null;
                boolean isMain = request.isForMainFrame();

                Slog.w(TAG, "WebView onReceivedError: code=" + code + ", desc=" + desc + ", isMain=" + isMain + ", url=" + fUrl);
                if (isMain && desc.contains("ERR_CACHE_MISS") && fUrl != null) {
                    Slog.w(TAG, "Recovering from ERR_CACHE_MISS by explicitly loading URL");
                    view.post(() -> {
                        view.loadUrl(fUrl);
                    });
                }
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
            if (origClient != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    origClient.onReceivedHttpError(view, request, errorResponse);
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }
        }

        @Override
        public void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend) {
            Slog.i(TAG, "WebView onFormResubmission: forcing resend to prevent net::ERR_CACHE_MISS");
            if (resend != null) {
                resend.sendToTarget();
            } else {
                super.onFormResubmission(view, dontResend, resend);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            if (origClient != null) {
                try {
                    origClient.doUpdateVisitedHistory(view, url, isReload);
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }
        }
    }

    public static class BlackBoxWrappedChromeClient extends android.webkit.WebChromeClient {
        public final android.webkit.WebChromeClient origClient;

        public BlackBoxWrappedChromeClient(android.webkit.WebChromeClient origClient) {
            this.origClient = origClient;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
            Slog.i(TAG, "WebView onCreateWindow called (popup/OAuth window)");
            if (resultMsg != null && resultMsg.obj instanceof WebView.WebViewTransport) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(view);
                resultMsg.sendToTarget();
                return true;
            }
            if (origClient != null) {
                try {
                    return origClient.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
                } catch (Throwable t) {
                    Slog.w(TAG, "origClient.onCreateWindow failed: " + t.getMessage());
                }
            }
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (origClient != null) {
                try {
                    origClient.onProgressChanged(view, newProgress);
                    return;
                } catch (Throwable ignored) {}
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (origClient != null) {
                try {
                    origClient.onReceivedTitle(view, title);
                    return;
                } catch (Throwable ignored) {}
            }
            super.onReceivedTitle(view, title);
        }
    }

    private static android.webkit.WebResourceResponse handleInterceptRequest(String urlStr, String method, Map<String, String> requestHeaders) {
        if (urlStr == null) return null;
        try {
            // NEVER fetch or serve Tencent error pages! Return empty 204 response
            if (urlStr.contains("itop_webview_error.html") || urlStr.contains("itop_webview_error") || urlStr.contains("errorCode=")) {
                Slog.w(TAG, "Blocking error page request: " + urlStr);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return new android.webkit.WebResourceResponse("text/html", "UTF-8", 204, "No Content", Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
                } else {
                    return new android.webkit.WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(new byte[0]));
                }
            }

            // Allow Chromium to handle all OAuth logins, POST requests, and redirects directly
            return null;
        } catch (Throwable t) {
            Slog.w(TAG, "handleInterceptRequest error for " + urlStr + ": " + t.getMessage());
            return null;
        }
    }
    
    private static boolean isCacheOrOAuthError(int code, String desc, String url) {
        if (code == -1) return true; // ERROR_UNKNOWN or ERR_CACHE_MISS
        if (desc != null) {
            String lower = desc.toLowerCase();
            if (lower.contains("cache") || lower.contains("err_cache_miss") || lower.contains("net::")) {
                return true;
            }
        }
        if (url != null) {
            String lowerUrl = url.toLowerCase();
            if (lowerUrl.contains("globh.com") || lowerUrl.contains("discord") || lowerUrl.contains("thirdlogin")
                    || lowerUrl.contains("appleid") || lowerUrl.contains("facebook") || lowerUrl.contains("twitter")) {
                return true;
            }
        }
        return false;
    }

    private static void applyWebViewFixes(final WebView webView, final String fallbackUrl) {
        try {
            android.webkit.WebSettings ws = webView.getSettings();
            if (ws != null) {
                // Use LOAD_DEFAULT so Chromium preserves POST redirects and cached resources safely
                ws.setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
                ws.setDomStorageEnabled(true);
                ws.setDatabaseEnabled(true);
                ws.setJavaScriptEnabled(true);
                ws.setJavaScriptCanOpenWindowsAutomatically(true);
                ws.setAllowFileAccess(true);
                ws.setAllowContentAccess(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ws.setAllowFileAccessFromFileURLs(true);
                    ws.setAllowUniversalAccessFromFileURLs(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ws.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        ws.setSafeBrowsingEnabled(false);
                    } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ws.setMediaPlaybackRequiresUserGesture(false);
                }
                ws.setBlockNetworkLoads(false);
                ws.setBlockNetworkImage(false);

                try {
                    File webviewCache = new File(webView.getContext().getCacheDir(), "webview");
                    if (!webviewCache.exists()) webviewCache.mkdirs();
                    java.lang.reflect.Method setAppCacheEnabled = ws.getClass().getMethod("setAppCacheEnabled", boolean.class);
                    setAppCacheEnabled.invoke(ws, true);
                    java.lang.reflect.Method setAppCachePath = ws.getClass().getMethod("setAppCachePath", String.class);
                    setAppCachePath.invoke(ws, webviewCache.getAbsolutePath());
                    java.lang.reflect.Method setDatabasePath = ws.getClass().getMethod("setDatabasePath", String.class);
                    setDatabasePath.invoke(ws, webviewCache.getAbsolutePath());
                } catch (Throwable ignored) {}

                String ua = ws.getUserAgentString();
                if (ua != null) {
                    if (ua.contains("BlackBox")) {
                        ua = ua.replace(" BlackBox", "").trim();
                    }
                    // Strip WebView markers so Google OAuth (accounts.google.com) and other identity providers do not block WebView with 403 disallowed_useragent
                    ua = ua.replace("; wv", "");
                    ua = ua.replaceAll("Version/\\d+\\.\\d+\\s*", "");
                    ws.setUserAgentString(ua);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    WebView.setWebContentsDebuggingEnabled(true);
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            }

            try {
                android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
                cm.setAcceptCookie(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cm.setAcceptThirdPartyCookies(webView, true);
                }
                cm.flush();
            } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }

            // Continuously wrap any client set on the WebView with BlackBoxWrappedClient and BlackBoxWrappedChromeClient
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    android.webkit.WebViewClient currentClient = webView.getWebViewClient();
                    if (!(currentClient instanceof BlackBoxWrappedClient)) {
                        webView.setWebViewClient(new BlackBoxWrappedClient(currentClient));
                    }
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
                try {
                    android.webkit.WebChromeClient currentChrome = webView.getWebChromeClient();
                    if (!(currentChrome instanceof BlackBoxWrappedChromeClient)) {
                        webView.setWebChromeClient(new BlackBoxWrappedChromeClient(currentChrome));
                    }
                } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
            } else {
                try {
                    java.lang.reflect.Method m = webView.getClass().getMethod("getWebViewClient");
                    android.webkit.WebViewClient currentClient = (android.webkit.WebViewClient) m.invoke(webView);
                    if (!(currentClient instanceof BlackBoxWrappedClient)) {
                        webView.setWebViewClient(new BlackBoxWrappedClient(currentClient));
                    }
                } catch (Throwable t) {
                    webView.setWebViewClient(new BlackBoxWrappedClient(null));
                }
            }

            // If the WebView is currently displaying the error page or blank, recover with fallbackUrl!
            String curUrl = webView.getUrl();
            if (curUrl != null && (curUrl.contains("itop_webview_error.html") || curUrl.contains("errorCode="))) {
                Slog.i(TAG, "WebView was hijacked by error page (" + curUrl + "), recovering with fallback: " + fallbackUrl);
                webView.stopLoading();
                if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                    webView.loadUrl(fallbackUrl);
                }
            } else if (curUrl == null || curUrl.isEmpty() || curUrl.equals("about:blank")) {
                if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                    Slog.i(TAG, "WebView URL is blank, loading fallbackUrl: " + fallbackUrl);
                    webView.loadUrl(fallbackUrl);
                }
            }
        } catch (Throwable ignored) {
            Slog.e(TAG, "Error in scanAllRootViews", ignored);
        }
    }

    
    public static void ensureActivityContext(Activity activity) {
        if (activity == null) {
            return;
        }
        
        try {
            
            Context currentContext = activity.getBaseContext();
            if (currentContext != null) {
                Slog.d(TAG, "Activity already has context: " + activity.getClass().getName());
                return;
            }
            
            Slog.w(TAG, "Activity has null context, ensuring valid context: " + activity.getClass().getName());
            
            
            Context validContext = null;
            try {
                validContext = getApplication();
                if (validContext == null) {
                    validContext = BlackBoxCore.getContext();
                }
            } catch (Exception e) {
                Slog.w(TAG, "Could not get application context: " + e.getMessage());
                validContext = BlackBoxCore.getContext();
            }
            
            if (validContext != null) {
                
                try {
                    Context packageContext = validContext.createPackageContext(
                        activity.getPackageName(),
                        Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
                    );
                    
                    
                    java.lang.reflect.Method attachBaseContext = Activity.class.getDeclaredMethod("attachBaseContext", Context.class);
                    attachBaseContext.setAccessible(true);
                    attachBaseContext.invoke(activity, packageContext);
                    Slog.d(TAG, "Successfully attached package context to activity: " + activity.getClass().getName());
                } catch (Exception e) {
                    Slog.w(TAG, "Could not attach base context to activity: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error ensuring activity context: " + e.getMessage());
        }
    }

    
    public static void hookActivityThread() {
        try {
            
            Object activityThread = BlackBoxCore.mainThread();
            if (activityThread != null) {
                
                Instrumentation instrumentation = BRActivityThread.get(activityThread).mInstrumentation();
                if (instrumentation != null) {
                    Slog.d(TAG, "Found ActivityThread instrumentation, ensuring it's our AppInstrumentation");
                    
                    
                    if (!(instrumentation instanceof AppInstrumentation)) {
                        Slog.w(TAG, "ActivityThread instrumentation is not our AppInstrumentation, attempting to replace");
                        
                        
                        try {
                            AppInstrumentation appInstrumentation = AppInstrumentation.get();
                            appInstrumentation.injectHook();
                            Slog.d(TAG, "Successfully replaced ActivityThread instrumentation with AppInstrumentation");
                        } catch (Exception e) {
                            Slog.w(TAG, "Could not replace ActivityThread instrumentation: " + e.getMessage());
                        }
                    } else {
                        Slog.d(TAG, "ActivityThread instrumentation is already our AppInstrumentation");
                    }
                } else {
                    Slog.w(TAG, "ActivityThread instrumentation is null");
                }
            } else {
                Slog.w(TAG, "ActivityThread is null");
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error hooking ActivityThread: " + e.getMessage());
        }
    }

    
    private Application createMinimalApplication(Context packageContext, String packageName) {
        try {
            Slog.d(TAG, "Creating minimal application for " + packageName);
            
            
            Application app = new Application() {
                @Override
                public void onCreate() {
                    super.onCreate();
                    Slog.d(TAG, "Minimal application onCreate called for " + packageName);
                }
                
                @Override
                public String getPackageName() {
                    return packageName;
                }
                
                @Override
                public Context getApplicationContext() {
                    return this;
                }
            };
            
            
            if (packageContext != null) {
                try {
                    Method attachBaseContext = Application.class.getDeclaredMethod("attachBaseContext", Context.class);
                    attachBaseContext.setAccessible(true);
                    attachBaseContext.invoke(app, packageContext);
                    Slog.d(TAG, "Successfully attached base context to minimal application for " + packageName);
                } catch (Exception e) {
                    Slog.w(TAG, "Could not attach base context to minimal application: " + e.getMessage());
                }
            } else {
                Slog.w(TAG, "Package context is null, cannot attach base context to minimal application");
            }
            
            Slog.d(TAG, "Minimal application created successfully for " + packageName);
            return app;
        } catch (Exception e) {
            Slog.e(TAG, "Error creating minimal application for " + packageName, e);
            return null;
        }
    }

    public static class AppBindData {
        String processName;
        ApplicationInfo appInfo;
        List<ProviderInfo> providers;
        Object info;
    }
}


















