package top.niunaijun.blackbox.core;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.entity.pm.InstallResult;

public class GmsCore {
    private static final String TAG = "GmsCore";

    private static final HashSet<String> GOOGLE_APP = new HashSet<>();
    private static final HashSet<String> GOOGLE_SERVICE = new HashSet<>();
    public static final String GMS_PKG = "com.google.android.gms";
    public static final String GSF_PKG = "com.google.android.gsf";
    public static final String VENDING_PKG = "com.android.vending";

    public static final String OFFICIAL_GMS_SIGNATURE = "3082020d30820176a0030201020204420e6b8b300d06092a864886f70d0101050500303111300f06035504031308416e64726f69643110300e060355040b1307416e64726f69643110300e060355040613025553301e170d033038303631343033313532345a270d3334303632323033313532345a303111300f06035504031308416e64726f69643110300e060355040b1307416e64726f69643110300e06035504061302555330819f300d06092a864886f70d010101050003818d0030818902818100b3967d32a4e21a206b0d9f4856f6e3c0ee0a3a7894a4ef6e885c3945f34177d6ee0d4948a313d528b14421b8a531e21b71217e57c6ed0013f9f60f64c01d9f0f9b6b7a5a8e0f117c2f0f421f1d7d0a2ec7039a5db7e94e240fae41d8e1215443a53e4b7e8499250f28e20257e84732101056efeeef8d4b31a89c445a4a49c9510203010001";

    static {
        GOOGLE_APP.add(VENDING_PKG);
        GOOGLE_APP.add("com.google.android.play.games");
        GOOGLE_APP.add("com.google.android.wearable.app");
        GOOGLE_APP.add("com.google.android.wearable.app.cn");

        GOOGLE_SERVICE.add(GMS_PKG);
        GOOGLE_SERVICE.add(GSF_PKG);
        GOOGLE_SERVICE.add("com.google.android.gsf.login");
        GOOGLE_SERVICE.add("com.google.android.backuptransport");
        GOOGLE_SERVICE.add("com.google.android.backup");
        GOOGLE_SERVICE.add("com.google.android.configupdater");
        GOOGLE_SERVICE.add("com.google.android.syncadapters.contacts");
        GOOGLE_SERVICE.add("com.google.android.feedback");
        GOOGLE_SERVICE.add("com.google.android.onetimeinitializer");
        GOOGLE_SERVICE.add("com.google.android.partnersetup");
        GOOGLE_SERVICE.add("com.google.android.setupwizard");
        GOOGLE_SERVICE.add("com.google.android.syncadapters.calendar");
    }

    public static boolean isGoogleService(String packageName) {
        return GOOGLE_SERVICE.contains(packageName);
    }

    public static boolean isGoogleAppOrService(String str) {
        return GOOGLE_APP.contains(str) || GOOGLE_SERVICE.contains(str);
    }

    private static InstallResult installPackages(Set<String> list, int userId) {
        BlackBoxCore blackBoxCore = BlackBoxCore.get();
        for (String packageName : list) {
            if (blackBoxCore.isInstalled(packageName, userId)) {
                continue;
            }
            try {
                BlackBoxCore.getContext().getPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }
            InstallResult installResult = blackBoxCore.installPackageAsUser(packageName, userId);
            if (!installResult.success) {
                Slog.e(TAG, "Failed to install " + packageName + ": " + installResult.msg);
            }
        }
        return new InstallResult();
    }

    private static void uninstallPackages(Set<String> list, int userId) {
        BlackBoxCore blackBoxCore = BlackBoxCore.get();
        for (String packageName : list) {
            blackBoxCore.uninstallPackageAsUser(packageName, userId);
        }
    }

    public static InstallResult installGApps(int userId) {
        if (isInstalledGoogleService(userId)) {
            Slog.i(TAG, "GMS is already installed for user " + userId);
            return new InstallResult();
        }
        Context ctx = BlackBoxCore.getContext();
        if (ctx != null) {
            installMicroGFromAssets(ctx, userId);
        }
        if (isInstalledGoogleService(userId)) {
            Slog.i(TAG, "GMS is successfully installed via MicroG assets for user " + userId);
            return new InstallResult();
        }

        // Search common local paths (e.g. Downloads folder) for MicroG / GmsCore APK
        try {
            File[] searchDirs = new File[]{
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    ctx != null ? ctx.getExternalFilesDir(null) : null,
                    ctx != null ? ctx.getFilesDir() : null
            };
            String[] candidateNames = new String[]{"GmsCore.apk", "microg.apk", "com.google.android.gms.apk", "FakeStore.apk"};
            for (File dir : searchDirs) {
                if (dir == null || !dir.exists()) continue;
                for (String name : candidateNames) {
                    File candidate = new File(dir, name);
                    if (candidate.exists() && candidate.isFile()) {
                        Slog.i(TAG, "installGApps found local APK: " + candidate.getAbsolutePath());
                        InstallResult r = BlackBoxCore.get().installPackageAsUser(candidate, userId);
                        if (r.success) {
                            Slog.i(TAG, "Successfully installed " + name + " from " + candidate.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Slog.w(TAG, "Search local candidate APKs error: " + t.getMessage());
        }

        if (isInstalledGoogleService(userId)) {
            Slog.i(TAG, "GMS is successfully installed via local storage for user " + userId);
            return new InstallResult();
        }

        Set<String> coreApps = new HashSet<>();
        coreApps.add(GMS_PKG);
        coreApps.add(GSF_PKG);
        coreApps.add(VENDING_PKG);
        coreApps.add("com.google.android.play.games");

        InstallResult installResult = installPackages(coreApps, userId);
        if (isInstalledGoogleService(userId)) {
            return new InstallResult();
        }

        if (!installResult.success) {
            Slog.e(TAG, "Some GApps failed to install: " + installResult.msg);
            return installResult;
        }

        InstallResult failResult = new InstallResult();
        failResult.installError("GMS/MicroG APK not found. Please place GmsCore.apk in your Downloads folder or install MicroG APK.");
        return failResult;
    }

    public static void uninstallGApps(int userId) {
        uninstallPackages(GOOGLE_SERVICE, userId);
        uninstallPackages(GOOGLE_APP, userId);
    }

    public static void remove(String packageName) {
        GOOGLE_SERVICE.remove(packageName);
        GOOGLE_APP.remove(packageName);
    }

    public static InstallResult installMicroG(File gmsCoreApk, File fakeStoreApk, int userId) {
        BlackBoxCore blackBoxCore = BlackBoxCore.get();
        InstallResult result = new InstallResult();
        if (gmsCoreApk != null && gmsCoreApk.exists()) {
            InstallResult r1 = blackBoxCore.installPackageAsUser(gmsCoreApk, userId);
            if (!r1.success) {
                Slog.e(TAG, "Failed to install GmsCore: " + r1.msg);
                return r1;
            }
        }
        if (fakeStoreApk != null && fakeStoreApk.exists()) {
            InstallResult r2 = blackBoxCore.installPackageAsUser(fakeStoreApk, userId);
            if (!r2.success) {
                Slog.e(TAG, "Failed to install FakeStore: " + r2.msg);
                return r2;
            }
        }
        return result;
    }

    public static InstallResult installMicroGFromAssets(Context context, int userId) {
        if (context == null) return new InstallResult();
        InstallResult lastResult = new InstallResult();
        try {
            File cacheDir = context.getCacheDir();
            for (String apkName : new String[]{"GmsCore.apk", "FakeStore.apk"}) {
                String targetPkg = "GmsCore.apk".equals(apkName) ? GMS_PKG : VENDING_PKG;
                if (BlackBoxCore.get().isInstalled(targetPkg, userId)) {
                    Slog.i(TAG, "installMicroGFromAssets: " + targetPkg + " is already installed for user " + userId);
                    continue;
                }
                File outApk = new File(cacheDir, apkName);
                try (InputStream is = context.getAssets().open(apkName)) {
                    FileUtils.copyFile(is, outApk);
                    if (outApk.exists() && outApk.length() > 0) {
                        Slog.i(TAG, "Extracted asset " + apkName + " size: " + outApk.length() + " bytes. Installing...");
                        InstallResult r = BlackBoxCore.get().installPackageAsUser(outApk, userId);
                        Slog.i(TAG, "installMicroGFromAssets installed " + apkName + " -> success=" + r.success + " msg=" + r.msg);
                        lastResult = r;
                    } else {
                        Slog.e(TAG, "Extracted asset " + apkName + " failed or is 0 bytes!");
                    }
                } catch (Throwable t) {
                    Slog.e(TAG, "installMicroGFromAssets error processing " + apkName + ": " + t.getMessage(), t);
                } finally {
                    try {
                        if (outApk.exists()) outApk.delete();
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            Slog.e(TAG, "installMicroGFromAssets global error: " + t.getMessage(), t);
        }
        return lastResult;
    }

    public static boolean isSupportGms() {
        return true;
    }

    public static boolean isInstalledGoogleService(int userId) {
        return BlackBoxCore.get().isInstalled(GMS_PKG, userId);
    }
}
