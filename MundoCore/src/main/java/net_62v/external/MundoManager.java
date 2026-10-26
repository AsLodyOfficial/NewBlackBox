package net_62v.external;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.IBinder;

import com.mundo.MundoCore;
import com.mundo.app.configuration.AppLifecycleCallback;
import com.mundo.app.configuration.ClientConfiguration;
import com.mundo.core.system.user.BUserInfo;
import com.mundo.entity.pm.InstallResult;
import com.mundo.entity.pm.InstalledModule;

import java.io.File;
import java.util.List;

/**
 * MundoManager — Public API wrapper for MundoCore.
 * All MundoCore functionality exposed as static methods.
 * Usage: MundoManager.launchApk("com.example.app", 0);
 */
public class MundoManager {

    // ─────────────────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────────────────

    public static void attachBaseContext(Context context, ClientConfiguration config) {
        MundoCore.get().doAttachBaseContext(context, config);
    }

    public static void create() {
        MundoCore.get().doCreate();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CONTEXT & SYSTEM
    // ─────────────────────────────────────────────────────────────────────────

    public static Context getContext() {
        return MundoCore.getContext();
    }

    public static String getHostPackageName() {
        return MundoCore.getHostPkg();
    }

    public static int getHostUid() {
        return MundoCore.getHostUid();
    }

    public static int getHostUserId() {
        return MundoCore.getHostUserId();
    }

    public static android.content.pm.PackageManager getPackageManager() {
        return MundoCore.getPackageManager();
    }

    public static boolean is64Bit() {
        return MundoCore.is64Bit();
    }

    public static boolean checkSelfPermission(String permission) {
        return MundoCore.get().checkSelfPermission(permission);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PROCESS TYPE
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isMainProcess() {
        return MundoCore.get().isMainProcess();
    }

    public static boolean isServerProcess() {
        return MundoCore.get().isServerProcess();
    }

    public static boolean isBlackProcess() {
        return MundoCore.get().isBlackProcess();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SETTINGS
    // ─────────────────────────────────────────────────────────────────────────

    public static void setHideRoot(boolean hide) {
        MundoCore.setHideRoot(hide);
    }

    public static void setHideXposed(boolean hide) {
        MundoCore.setHideXposed(hide);
    }

    public static void setEnableDaemonService(boolean enable) {
        MundoCore.setEnableDaemonService(enable);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ACTIVITY / LAUNCH
    // ─────────────────────────────────────────────────────────────────────────

    public static void startActivity(Intent intent, int userId) {
        MundoCore.get().startActivity(intent, userId);
    }

    public static boolean launchApk(String packageName, int userId) {
        return MundoCore.get().launchApk(packageName, userId);
    }

    public static void onBeforeMainLaunchApk(String packageName, int userId) {
        MundoCore.get().onBeforeMainLaunchApk(packageName, userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PACKAGE INSTALL / UNINSTALL
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isInstalled(String packageName, int userId) {
        return MundoCore.get().isInstalled(packageName, userId);
    }

    public static InstallResult installPackage(String packageName, int userId) {
        return MundoCore.get().installPackageAsUser(packageName, userId);
    }

    public static InstallResult installPackage(File apk, int userId) {
        return MundoCore.get().installPackageAsUser(apk, userId);
    }

    public static InstallResult installPackage(Uri apk, int userId) {
        return MundoCore.get().installPackageAsUser(apk, userId);
    }

    public static void uninstallPackage(String packageName) {
        MundoCore.get().uninstallPackage(packageName);
    }

    public static void uninstallPackageAsUser(String packageName, int userId) {
        MundoCore.get().uninstallPackageAsUser(packageName, userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  APP INFO
    // ─────────────────────────────────────────────────────────────────────────

    public static ApplicationInfo getApplicationInfo(String packageName) {
        return MundoCore.get().getApplicationInfo(packageName);
    }

    public static List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return MundoCore.get().getInstalledApplications(flags, userId);
    }

    public static List<PackageInfo> getInstalledPackages(int flags, int userId) {
        return MundoCore.get().getInstalledPackages(flags, userId);
    }

    public static boolean isAppRunning(String packageName, int userId) {
        return MundoCore.get().isAppRunning(packageName, userId);
    }

    public static void clearPackage(String packageName, int userId) {
        MundoCore.get().clearPackage(packageName, userId);
    }

    public static void stopPackage(String packageName, int userId) {
        MundoCore.get().stopPackage(packageName, userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  XPOSED / MODULE
    // ─────────────────────────────────────────────────────────────────────────

    public static InstallResult installXPModule(File apk) {
        return MundoCore.get().installXPModule(apk);
    }

    public static InstallResult installXPModule(Uri apk) {
        return MundoCore.get().installXPModule(apk);
    }

    public static InstallResult installXPModule(String packageName) {
        return MundoCore.get().installXPModule(packageName);
    }

    public static void uninstallXPModule(String packageName) {
        MundoCore.get().uninstallXPModule(packageName);
    }

    public static boolean isXPEnable() {
        return MundoCore.get().isXPEnable();
    }

    public static void setXPEnable(boolean enable) {
        MundoCore.get().setXPEnable(enable);
    }

    public static boolean isXposedModule(File file) {
        return MundoCore.get().isXposedModule(file);
    }

    public static boolean isInstalledXposedModule(String packageName) {
        return MundoCore.get().isInstalledXposedModule(packageName);
    }

    public static boolean isModuleEnable(String packageName) {
        return MundoCore.get().isModuleEnable(packageName);
    }

    public static void setModuleEnable(String packageName, boolean enable) {
        MundoCore.get().setModuleEnable(packageName, enable);
    }

    public static List<InstalledModule> getInstalledXPModules() {
        return MundoCore.get().getInstalledXPModules();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  USER MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    public static List<BUserInfo> getUsers() {
        return MundoCore.get().getUsers();
    }

    public static BUserInfo createUser(int userId) {
        return MundoCore.get().createUser(userId);
    }

    public static void deleteUser(int userId) {
        MundoCore.get().deleteUser(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LIFECYCLE CALLBACKS
    // ─────────────────────────────────────────────────────────────────────────

    public static void addAppLifecycleCallback(AppLifecycleCallback callback) {
        MundoCore.get().addAppLifecycleCallback(callback);
    }

    public static void removeAppLifecycleCallback(AppLifecycleCallback callback) {
        MundoCore.get().removeAppLifecycleCallback(callback);
    }

    public static List<AppLifecycleCallback> getAppLifecycleCallbacks() {
        return MundoCore.get().getAppLifecycleCallbacks();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GMS
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isSupportGms() {
        return MundoCore.get().isSupportGms();
    }

    public static boolean isInstallGms(int userId) {
        return MundoCore.get().isInstallGms(userId);
    }

    public static InstallResult installGms(int userId) {
        return MundoCore.get().installGms(userId);
    }

    public static boolean uninstallGms(int userId) {
        return MundoCore.get().uninstallGms(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SERVICE
    // ─────────────────────────────────────────────────────────────────────────

    public static IBinder getService(String name) {
        return MundoCore.get().getService(name);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  EXCEPTION HANDLER
    // ─────────────────────────────────────────────────────────────────────────

    public static void setExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        MundoCore.get().setExceptionHandler(handler);
    }

    public static Thread.UncaughtExceptionHandler getExceptionHandler() {
        return MundoCore.get().getExceptionHandler();
    }
}