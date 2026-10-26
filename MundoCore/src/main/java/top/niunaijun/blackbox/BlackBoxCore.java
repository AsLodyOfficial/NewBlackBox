package top.niunaijun.blackbox;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.mundo.MundoCore;
import com.mundo.app.configuration.AppLifecycleCallback;
import com.mundo.app.configuration.ClientConfiguration;
import com.mundo.core.system.user.BUserInfo;
import com.mundo.entity.pm.InstallResult;
import com.mundo.fake.frameworks.BActivityManager;
import com.mundo.fake.frameworks.BJobManager;
import com.mundo.fake.frameworks.BPackageManager;
import com.mundo.fake.frameworks.BStorageManager;

import java.io.File;
import java.util.List;

public class BlackBoxCore {
    private static final BlackBoxCore sInstance = new BlackBoxCore();

    public static BlackBoxCore get() {
        return sInstance;
    }

    public static Context getContext() {
        return MundoCore.getContext();
    }

    public static PackageManager getPackageManager() {
        return MundoCore.getPackageManager();
    }

    public static String getHostPkg() {
        return MundoCore.getHostPkg();
    }

    public static int getHostUid() {
        return MundoCore.getHostUid();
    }

    public static int getHostUserId() {
        return MundoCore.getHostUserId();
    }

    public static BPackageManager getBPackageManager() {
        return MundoCore.getBPackageManager();
    }

    public static BActivityManager getBActivityManager() {
        return MundoCore.getBActivityManager();
    }

    public static BJobManager getBJobManager() {
        return MundoCore.getBJobManager();
    }

    public static BStorageManager getBStorageManager() {
        return MundoCore.getBStorageManager();
    }

    public static boolean is64Bit() {
        return MundoCore.is64Bit();
    }

    public void closeCodeInit() {
    }

    public void onBeforeMainApplicationAttach(Application application, Context base) {
    }

    public void onAfterMainApplicationAttach(Application application, Context base) {
    }

    public void onBeforeMainActivityOnCreate(Activity activity) {
    }

    public void onAfterMainActivityOnCreate(Activity activity) {
    }

    public void doAttachBaseContext(Context context, ClientConfiguration clientConfiguration) {
        MundoCore.get().doAttachBaseContext(context, clientConfiguration);
    }

    public void doCreate() {
        MundoCore.get().doCreate();
    }

    public void addAppLifecycleCallback(AppLifecycleCallback appLifecycleCallback) {
        MundoCore.get().addAppLifecycleCallback(appLifecycleCallback);
    }

    public void removeAppLifecycleCallback(AppLifecycleCallback appLifecycleCallback) {
        MundoCore.get().removeAppLifecycleCallback(appLifecycleCallback);
    }

    public void addServiceAvailableCallback(Runnable callback) {
        if (callback != null) {
            callback.run();
        }
    }

    public boolean isSupportGms() {
        return MundoCore.get().isSupportGms();
    }

    public boolean isInstallGms(int userId) {
        return MundoCore.get().isInstallGms(userId);
    }

    public InstallResult installGms(int userId) {
        return MundoCore.get().installGms(userId);
    }

    public boolean uninstallGms(int userId) {
        return MundoCore.get().uninstallGms(userId);
    }

    public InstallResult installPackageAsUser(Uri uri, int userId) {
        return MundoCore.get().installPackageAsUser(uri, userId);
    }

    public InstallResult installPackageAsUser(File file, int userId) {
        return MundoCore.get().installPackageAsUser(file, userId);
    }

    public InstallResult installPackageAsUser(String packageName, int userId) {
        return MundoCore.get().installPackageAsUser(packageName, userId);
    }

    public void uninstallPackageAsUser(String packageName, int userId) {
        MundoCore.get().uninstallPackageAsUser(packageName, userId);
    }

    public boolean launchApk(String packageName, int userId) {
        return MundoCore.get().launchApk(packageName, userId);
    }

    public void clearPackage(String packageName, int userId) {
        MundoCore.get().clearPackage(packageName, userId);
    }

    public void stopPackage(String packageName, int userId) {
        MundoCore.get().stopPackage(packageName, userId);
    }

    public boolean isInstalled(String packageName, int userId) {
        return MundoCore.get().isInstalled(packageName, userId);
    }

    public List<BUserInfo> getUsers() {
        return MundoCore.get().getUsers();
    }

    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return MundoCore.get().getInstalledApplications(flags, userId);
    }

    public void deleteUser(int userId) {
        MundoCore.get().deleteUser(userId);
    }

    public boolean isBlackBoxApp(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        return packageName.equals(getHostPkg());
    }

    public boolean isBlackBoxApp(ApplicationInfo info) {
        if (info == null) {
            return false;
        }
        return isBlackBoxApp(info.packageName);
    }

    public boolean isBlackBoxApp(File apkFile) {
        return false;
    }

    public interface LogSendListener {
        void onSuccess();
        void onFailure(String error);
    }

    public void sendLogs(String tag, boolean upload, LogSendListener listener) {
        if (listener != null) {
            listener.onSuccess();
        }
    }
}
