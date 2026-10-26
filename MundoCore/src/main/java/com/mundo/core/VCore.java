package com.mundo.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.mundo.MundoCore;
import com.mundo.app.BActivityThread;
import com.mundo.core.env.BEnvironment;
import com.mundo.utils.FileUtils;
import com.mundo.utils.TrieTree;

@SuppressLint("SdCardPath")
public class VCore {
    public static final String TAG = "VCore";

    private static final VCore sVCore = new VCore();
    private static final TrieTree mTrieTree = new TrieTree();
    private static final TrieTree sBlackTree = new TrieTree();
    private final Map<String, String> mRedirectMap = new LinkedHashMap<>();

    public static VCore get() {
        return sVCore;
    }

    // /data/data/com.google/  ----->  /data/data/com.virtual/data/com.google/
    public void addRedirect(String origPath, String redirectPath) {
        if (TextUtils.isEmpty(origPath) || TextUtils.isEmpty(redirectPath) || mRedirectMap.get(origPath) != null)
            return;
        //Add the key to TrieTree
        mTrieTree.add(origPath);
        mRedirectMap.put(origPath, redirectPath);
        File redirectFile = new File(redirectPath);
        if (!redirectFile.exists()) {
            FileUtils.mkdirs(redirectPath);
        }
        VNative.addIORule(origPath, redirectPath);
    }

    public void addBlackRedirect(String path) {
        if (TextUtils.isEmpty(path))
            return;
        sBlackTree.add(path);
        VNative.addIORule(path, path);
    }

    public String redirectPath(String path) {
        if (TextUtils.isEmpty(path))
            return path;
        if (path.contains("/SdCard/") || path.endsWith("/SdCard") || path.contains("/EliteCore/") || path.contains("/blackbox/")) {
            return path;
        }
        for (String dest : mRedirectMap.values()) {
            if (path.startsWith(dest)) {
                return path;
            }
        }
        String search = sBlackTree.search(path);
        if (!TextUtils.isEmpty(search))
            return path;

        //Search the key from TrieTree
        String key = mTrieTree.search(path);
        if (!TextUtils.isEmpty(key))
            path = path.replace(key, Objects.requireNonNull(mRedirectMap.get(key)));

        return path;
    }

    public File redirectPath(File path) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr));
    }

    public String redirectPath(String path, Map<String, String> rule) {
        if (TextUtils.isEmpty(path))
            return path;

        //Search the key from TrieTree
        String key = mTrieTree.search(path);
        if (!TextUtils.isEmpty(key))
            path = path.replace(key, Objects.requireNonNull(rule.get(key)));

        return path;
    }

    public File redirectPath(File path, Map<String, String> rule) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr, rule));
    }

    // 由于正常情况Application已完成重定向，以下重定向是怕代码写死。
    public void enableRedirect(Context context) {
        Map<String, String> rule = new LinkedHashMap<>();
        Set<String> blackRule = new HashSet<>();
        String packageName = context.getPackageName();

        try {
            ApplicationInfo packageInfo = MundoCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, BActivityThread.getUserId());
            int systemUserId = MundoCore.getHostUserId();
            rule.put(String.format("/data/data/%s/lib", packageName), packageInfo.nativeLibraryDir);
            rule.put(String.format("/data/user/%d/%s/lib", systemUserId, packageName), packageInfo.nativeLibraryDir);

            rule.put(String.format("/data/data/%s", packageName), packageInfo.dataDir);
            rule.put(String.format("/data/user/%d/%s", systemUserId, packageName), packageInfo.dataDir);
            File deDataDir = BEnvironment.getDeDataDir(packageName, BActivityThread.getUserId());
            FileUtils.mkdirs(deDataDir);
            FileUtils.mkdirs(new File(deDataDir, "databases"));
            FileUtils.mkdirs(new File(deDataDir, "files"));
            FileUtils.mkdirs(new File(deDataDir, "cache"));
            FileUtils.mkdirs(new File(deDataDir, "shared_prefs"));
            FileUtils.mkdirs(new File(deDataDir, "no_backup"));

            if ("com.google.android.gms".equals(packageName)) {
                try {
                    File chimeraDirDe = new File(deDataDir, "app_chimera");
                    FileUtils.mkdirs(chimeraDirDe);
                    File cfgDe = new File(chimeraDirDe, "current_config.fb");
                    if (!cfgDe.exists()) {
                        cfgDe.createNewFile();
                        cfgDe.setReadable(true, false);
                        cfgDe.setWritable(true, false);
                    }

                    File chimeraDirCe = new File(packageInfo.dataDir, "app_chimera");
                    FileUtils.mkdirs(chimeraDirCe);
                    File cfgCe = new File(chimeraDirCe, "current_config.fb");
                    if (!cfgCe.exists()) {
                        cfgCe.createNewFile();
                        cfgCe.setReadable(true, false);
                        cfgCe.setWritable(true, false);
                    }
                } catch (Exception ignored) {}
            }

            rule.put(String.format("/data/user_de/%d/%s", systemUserId, packageName), deDataDir.getAbsolutePath());

            // Ensure ART profile directories exist and redirect both current and reference profiles
            File profilesRoot = new File(BEnvironment.getVBoxRoot(), "profiles");
            FileUtils.mkdirs(profilesRoot.getAbsolutePath());
            // broad redirect as a safety net
            rule.put("/data/misc/profiles", profilesRoot.getAbsolutePath());

            File profilesCurDir = new File(profilesRoot, String.format("cur/%d/%s", BActivityThread.getUserId(), packageName));
            File profilesRefDir = new File(profilesRoot, String.format("ref/%d/%s", BActivityThread.getUserId(), packageName));
            FileUtils.mkdirs(profilesCurDir.getAbsolutePath());
            FileUtils.mkdirs(profilesRefDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/cur/%d/%s", BActivityThread.getUserId(), packageName), profilesCurDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/ref/%d/%s", BActivityThread.getUserId(), packageName), profilesRefDir.getAbsolutePath());

            try {
                File external = BEnvironment.getExternalStorageDirectory();
                FileUtils.mkdirs(external);
                File externalData = BEnvironment.getExternalDataDir(packageName);
                File externalObb = BEnvironment.getExternalObbDir(packageName);
                FileUtils.mkdirs(externalData);
                FileUtils.mkdirs(externalObb);
                File extFilesDir = BEnvironment.getExternalDataFilesDir(packageName);
                FileUtils.mkdirs(extFilesDir);
                FileUtils.mkdirs(BEnvironment.getExternalDataCacheDir(packageName));
                FileUtils.mkdirs(new File(extFilesDir, "contentcache/Compulsory/android"));
                FileUtils.mkdirs(new File(extFilesDir, "contentcache/Optional/android"));
                if (packageInfo.dataDir != null) {
                    File internalFiles = new File(packageInfo.dataDir, "files");
                    FileUtils.mkdirs(internalFiles);
                    FileUtils.mkdirs(new File(packageInfo.dataDir, "cache"));
                    FileUtils.mkdirs(new File(internalFiles, "contentcache/Compulsory/android"));
                    FileUtils.mkdirs(new File(internalFiles, "contentcache/Optional/android"));
                }

                // Smart OBB Passthrough for High-Performance Gaming:
                File hostObbPkg = new File(String.format("/storage/emulated/%d/Android/obb/%s", systemUserId, packageName));
                File[] virtualObbs = externalObb.listFiles();
                boolean hasVirtualObb = virtualObbs != null && virtualObbs.length > 0;
                if (!hasVirtualObb && hostObbPkg.exists()) {
                    blackRule.add("/sdcard/Android/obb/" + packageName);
                    blackRule.add(String.format("/storage/emulated/%d/Android/obb/%s", systemUserId, packageName));
                }

                // sdcard & primary storage redirection
                String extPath = external.getAbsolutePath();
                rule.put("/sdcard", extPath);
                rule.put(String.format("/storage/emulated/%d", systemUserId), extPath);

                blackRule.add("/sdcard/Pictures");
                blackRule.add(String.format("/storage/emulated/%d/Pictures", systemUserId));
                blackRule.add("/sdcard/DCIM");
                blackRule.add(String.format("/storage/emulated/%d/DCIM", systemUserId));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (MundoCore.get().setHideRoot()) {
                hideRoot(rule);
            }
            proc(rule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : rule.keySet()) {
            get().addRedirect(key, rule.get(key));
        }
        for (String s : blackRule) {
            get().addBlackRedirect(s);
        }
        VNative.enableIO();
    }

    private void hideRoot(Map<String, String> rule) {
        // Standard su paths
        rule.put("/system/app/Superuser.apk", "/system/app/Superuser.apk-fake");
        rule.put("/sbin/su", "/sbin/su-fake");
        rule.put("/system/bin/su", "/system/bin/su-fake");
        rule.put("/system/xbin/su", "/system/xbin/su-fake");
        rule.put("/data/local/xbin/su", "/data/local/xbin/su-fake");
        rule.put("/data/local/bin/su", "/data/local/bin/su-fake");
        rule.put("/system/sd/xbin/su", "/system/sd/xbin/su-fake");
        rule.put("/system/bin/failsafe/su", "/system/bin/failsafe/su-fake");
        rule.put("/data/local/su", "/data/local/su-fake");
        rule.put("/su/bin/su", "/su/bin/su-fake");
        
        // Magisk paths — anti-cheat scans these
        rule.put("/sbin/.magisk", "/sbin/.magisk-fake");
        rule.put("/data/adb/magisk", "/data/adb/magisk-fake");
        rule.put("/cache/.disable_magisk", "/cache/.disable_magisk-fake");
        rule.put("/dev/.magisk.unblock", "/dev/.magisk.unblock-fake");
        
        // Xposed paths
        rule.put("/system/framework/XposedBridge.jar", "/system/framework/XposedBridge.jar-fake");
        rule.put("/system/xbin/xposed", "/system/xbin/xposed-fake");
        rule.put("/data/data/de.robv.android.xposed.installer", "/data/data/de.robv.android.xposed.installer-fake");
        rule.put("/data/data/org.lsposed.manager", "/data/data/org.lsposed.manager-fake");
        
        // Virtual environment indicators — anti-cheat checks for these
        rule.put("/data/data/com.bly.dkplat", "/data/data/com.bly.dkplat-fake");
        rule.put("/data/data/com.by.tp.vp", "/data/data/com.by.tp.vp-fake");
        rule.put("/data/data/com.lbe.parallel", "/data/data/com.lbe.parallel-fake");
        rule.put("/data/data/com.excelliance.dualaid", "/data/data/com.excelliance.dualaid-fake");
        rule.put("/data/data/com.ludashi.dualspace", "/data/data/com.ludashi.dualspace-fake");
        rule.put("/data/data/com.polestar.multiaccount", "/data/data/com.polestar.multiaccount-fake");
        rule.put("/data/data/com.nox.mopen.app", "/data/data/com.nox.mopen.app-fake");
    }

    private void proc(Map<String, String> rule) {
        int appPid = BActivityThread.getAppPid();
        int pid = Process.myPid();
        String selfProc = "/proc/self/";
        String proc = "/proc/" + pid + "/";

        File procDir = BEnvironment.getProcDir(appPid);

        // ====================================================================
        // cmdline — already handled, redirects to fake cmdline with real pkg name
        // ====================================================================
        String cmdline = new File(procDir, "cmdline").getAbsolutePath();
        rule.put(proc + "cmdline", cmdline);
        rule.put(selfProc + "cmdline", cmdline);

        // ====================================================================
        // /proc/self/maps — CRITICAL: reveals host package paths, MundoCore lib
        // Anti-cheat reads this to find virtual env libraries in memory
        // ====================================================================
        File fakeMapFile = new File(procDir, "maps");
        createFakeProcFile(fakeMapFile, "maps");
        rule.put(proc + "maps", fakeMapFile.getAbsolutePath());
        rule.put(selfProc + "maps", fakeMapFile.getAbsolutePath());

        // ====================================================================
        // /proc/self/status — reveals TracerPid (non-zero = debugger attached)
        // Also reveals real name/pid which can differ from spoofed cmdline
        // ====================================================================
        File fakeStatusFile = new File(procDir, "status");
        createFakeProcStatus(fakeStatusFile);
        rule.put(proc + "status", fakeStatusFile.getAbsolutePath());
        rule.put(selfProc + "status", fakeStatusFile.getAbsolutePath());

        // ====================================================================
        // /proc/self/mountinfo & /proc/self/mounts — reveals virtual mount points
        // ====================================================================
        File fakeMountinfoFile = new File(procDir, "mountinfo");
        createFakeProcFile(fakeMountinfoFile, "mountinfo");
        rule.put(proc + "mountinfo", fakeMountinfoFile.getAbsolutePath());
        rule.put(selfProc + "mountinfo", fakeMountinfoFile.getAbsolutePath());

        File fakeMountsFile = new File(procDir, "mounts");
        createFakeProcFile(fakeMountsFile, "mounts");
        rule.put(proc + "mounts", fakeMountsFile.getAbsolutePath());
        rule.put(selfProc + "mounts", fakeMountsFile.getAbsolutePath());
    }

    /**
     * Create fake proc files that filter out virtual environment indicators.
     * We read the real file, strip entries containing our host package or 
     * virtual environment paths, then write the sanitized version.
     */
    private void createFakeProcFile(File fakeFile, String type) {
        try {
            FileUtils.mkdirs(fakeFile.getParent());
            int pid = Process.myPid();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream("/proc/" + pid + "/" + type)));
            StringBuilder sb = new StringBuilder();
            String line;
            String hostPkg = MundoCore.getHostPkg();
            while ((line = reader.readLine()) != null) {
                // Skip lines that reveal the virtual environment
                if (hostPkg != null && line.contains(hostPkg)) continue;
                if (line.contains("MundoCore")) continue;
                if (line.contains("mundo")) continue;
                if (line.contains("vbox")) continue;
                if (line.contains("BlackBox")) continue;
                if (line.contains("black_box")) continue;
                if (line.contains("virtual_core")) continue;
                if (line.contains("libbgmi.so")) continue;
                sb.append(line).append("\n");
            }
            reader.close();
            
            java.io.FileWriter writer = new java.io.FileWriter(fakeFile);
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            // If we can't create the fake file, just ignore — 
            // the redirect will fail and the real file is read
            Log.w(TAG, "Failed to create fake proc file: " + type, e);
        }
    }

    /**
     * Create a fake /proc/self/status that hides TracerPid and fixes Name/Pid
     */
    private void createFakeProcStatus(File fakeFile) {
        try {
            FileUtils.mkdirs(fakeFile.getParent());
            int pid = Process.myPid();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream("/proc/" + pid + "/status")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Force TracerPid to 0 — non-zero means debugger attached
                // Anti-cheat checks this to detect analysis tools
                if (line.startsWith("TracerPid:")) {
                    sb.append("TracerPid:\t0\n");
                    continue;
                }
                sb.append(line).append("\n");
            }
            reader.close();
            
            java.io.FileWriter writer = new java.io.FileWriter(fakeFile);
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            Log.w(TAG, "Failed to create fake proc status", e);
        }
    }
}
