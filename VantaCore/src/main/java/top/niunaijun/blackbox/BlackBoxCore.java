package top.niunaijun.blackbox;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import io.vanta.VantaCore;
import io.vanta.core.system.user.BUserInfo;
import io.vanta.entity.pm.InstallResult;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;
import java.io.File;
import java.util.List;

public class BlackBoxCore {
    private static final BlackBoxCore sInstance = new BlackBoxCore();

    public static BlackBoxCore get() {
        return sInstance;
    }

    public static Context getContext() {
        return VantaCore.getContext();
    }

    public static PackageManager getPackageManager() {
        return VantaCore.getPackageManager();
    }

    public static String getHostPkg() {
        return VantaCore.getHostPkg();
    }

    public static int getHostUid() {
        return VantaCore.getHostUid();
    }

    public static int getHostUserId() {
        return VantaCore.getHostUserId();
    }

    public static Object mainThread() {
        return VantaCore.mainThread();
    }

    public void closeCodeInit() {
        VantaCore.get().closeCodeInit();
    }

    public void onBeforeMainApplicationAttach(Application app, Context context) {
        VantaCore.get().onBeforeMainApplicationAttach(app, context);
    }

    public void onAfterMainApplicationAttach(Application app, Context context) {
        VantaCore.get().onAfterMainApplicationAttach(app, context);
    }

    public void onBeforeMainActivityOnCreate(Activity activity) {
        VantaCore.get().onBeforeMainActivityOnCreate(activity);
    }

    public void onAfterMainActivityOnCreate(Activity activity) {
        VantaCore.get().onAfterMainActivityOnCreate(activity);
    }

    public void doAttachBaseContext(Context context, io.vanta.app.configuration.ClientConfiguration clientConfiguration) {
        VantaCore.get().doAttachBaseContext(context, clientConfiguration);
    }

    public void doCreate() {
        VantaCore.get().doCreate();
    }

    public boolean isBlackBoxApp(String packageName) {
        return VantaCore.get().isNomadApp(packageName);
    }

    public boolean isBlackBoxApp(File apkFile) {
        return VantaCore.get().isNomadApp(apkFile);
    }

    public boolean isInstalled(String packageName, int userId) {
        return VantaCore.get().isInstalled(packageName, userId);
    }

    public List<BUserInfo> getUsers() {
        return VantaCore.get().getUsers();
    }

    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return VantaCore.get().getInstalledApplications(flags, userId);
    }

    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        return VantaCore.get().getInstalledPackages(flags, userId);
    }

    public InstallResult installPackageAsUser(String packageName, int userId) {
        return VantaCore.get().installPackageAsUser(packageName, userId);
    }

    public InstallResult installPackageAsUser(File apk, int userId) {
        return VantaCore.get().installPackageAsUser(apk, userId);
    }

    public InstallResult installPackageAsUser(Uri apk, int userId) {
        return VantaCore.get().installPackageAsUser(apk, userId);
    }

    public void uninstallPackageAsUser(String packageName, int userId) {
        VantaCore.get().uninstallPackageAsUser(packageName, userId);
    }

    public boolean launchApk(String packageName, int userId) {
        return VantaCore.get().launchApk(packageName, userId);
    }

    public void clearPackage(String packageName, int userId) {
        VantaCore.get().clearPackage(packageName, userId);
    }

    public void stopPackage(String packageName, int userId) {
        VantaCore.get().stopPackage(packageName, userId);
    }

    public void deleteUser(int userId) {
        VantaCore.get().deleteUser(userId);
    }

    public boolean isSupportGms() {
        return VantaCore.get().isSupportGms();
    }

    public boolean isInstallGms(int userId) {
        return VantaCore.get().isInstallGms(userId);
    }

    public InstallResult installGms(int userId) {
        return VantaCore.get().installGms(userId);
    }

    public boolean uninstallGms(int userId) {
        return VantaCore.get().uninstallGms(userId);
    }

    public void addServiceAvailableCallback(Runnable callback) {
        VantaCore.get().addServiceAvailableCallback(callback);
    }

    public void addAppLifecycleCallback(io.vanta.app.configuration.AppLifecycleCallback callback) {
        VantaCore.get().getAppLifecycleCallbacks().add(callback);
    }

    public void removeAppLifecycleCallback(io.vanta.app.configuration.AppLifecycleCallback callback) {
        VantaCore.get().getAppLifecycleCallbacks().remove(callback);
    }

    public boolean isBlackProcess() {
        return VantaCore.get().isBlackProcess();
    }

    public Handler getHandler() {
        return VantaCore.get().getHandler();
    }

    public void startActivity(Intent intent, int userId) {
        VantaCore.get().startActivity(intent, userId);
    }

    public void sendLogs(String caption, boolean async, final LogSendListener listener) {
        VantaCore.get().sendLogs(caption, async, new VantaCore.LogSendListener() {
            @Override
            public void onSuccess() {
                if (listener != null) listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                if (listener != null) listener.onFailure(error);
            }
        });
    }

    public interface LogSendListener {
        void onSuccess();
        void onFailure(String error);
    }
}
