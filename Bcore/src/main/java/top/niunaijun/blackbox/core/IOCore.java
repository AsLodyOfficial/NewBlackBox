package top.niunaijun.blackbox.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import top.niunaijun.blackbox.BlackBoxCore;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.TrieTree;


@SuppressLint("SdCardPath")
public class IOCore {
    public static final String TAG = "IOCore";

    private static final IOCore sIOCore = new IOCore();
    private static final TrieTree mTrieTree = new TrieTree();
    private static final TrieTree sBlackTree = new TrieTree();
    private final Map<String, String> mRedirectMap = new LinkedHashMap<>();

    private static final Map<String, Map<String, String>> sCachePackageRedirect = new HashMap<>();

    public static IOCore get() {
        return sIOCore;
    }

    
    public void addRedirect(String origPath, String redirectPath) {
        if (TextUtils.isEmpty(origPath) || TextUtils.isEmpty(redirectPath) || mRedirectMap.get(origPath) != null)
            return;
        
        mTrieTree.add(origPath);
        mRedirectMap.put(origPath, redirectPath);
        File redirectFile = new File(redirectPath);
        if (!redirectFile.exists()) {
            FileUtils.mkdirs(redirectPath);
        }
        NativeCore.addIORule(origPath, redirectPath);
    }

    public void addBlackRedirect(String path) {
        if (TextUtils.isEmpty(path))
            return;
        sBlackTree.add(path);
        NativeCore.addIORule(path, path);
    }

    public String redirectPath(String path) {
        if (TextUtils.isEmpty(path))
            return path;
        if (path.contains("/SdCard/") || path.endsWith("/SdCard") || path.contains("/blackbox/")) {
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
        if (path.contains("/SdCard/") || path.endsWith("/SdCard") || path.contains("/blackbox/")) {
            return path;
        }
        for (String dest : rule.values()) {
            if (path.startsWith(dest)) {
                return path;
            }
        }
        String search = sBlackTree.search(path);
        if (!TextUtils.isEmpty(search))
            return path;

        String key = mTrieTree.search(path);
        if (!TextUtils.isEmpty(key) && rule.containsKey(key))
            path = path.replace(key, Objects.requireNonNull(rule.get(key)));

        return path;
    }

    public File redirectPath(File path, Map<String, String> rule) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr, rule));
    }

    

    public void enableRedirect(Context context) {
        Map<String, String> rule = new LinkedHashMap<>();
        Set<String> blackRule = new HashSet<>();
        String packageName = context.getPackageName();
        String guestDataDir = null;

        try {
            ApplicationInfo packageInfo = BlackBoxCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, BlackBoxCore.getUserId());
            if (packageInfo != null) {
                guestDataDir = packageInfo.dataDir;
            }
            int systemUserId = BlackBoxCore.getHostUserId();
            rule.put(String.format("/data/data/%s/lib", packageName), packageInfo.nativeLibraryDir);
            rule.put(String.format("/data/user/%d/%s/lib", systemUserId, packageName), packageInfo.nativeLibraryDir);

            rule.put(String.format("/data/data/%s", packageName), packageInfo.dataDir);
            rule.put(String.format("/data/user/%d/%s", systemUserId, packageName), packageInfo.dataDir);

            
            File profilesRoot = new File(BEnvironment.getVirtualRoot(), "profiles");
            FileUtils.mkdirs(profilesRoot.getAbsolutePath());
            
            rule.put("/data/misc/profiles", profilesRoot.getAbsolutePath());

            File profilesCurDir = new File(profilesRoot, String.format("cur/%d/%s", BlackBoxCore.getUserId(), packageName));
            File profilesRefDir = new File(profilesRoot, String.format("ref/%d/%s", BlackBoxCore.getUserId(), packageName));
            FileUtils.mkdirs(profilesCurDir.getAbsolutePath());
            FileUtils.mkdirs(profilesRefDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/cur/%d/%s", BlackBoxCore.getUserId(), packageName), profilesCurDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/ref/%d/%s", BlackBoxCore.getUserId(), packageName), profilesRefDir.getAbsolutePath());

            File external = BEnvironment.getExternalStorageDirectory();
            FileUtils.mkdirs(external);
            File externalData = new File(external, "Android/data/" + packageName);
            File externalObb = new File(external, "Android/obb/" + packageName);
            FileUtils.mkdirs(externalData);
            FileUtils.mkdirs(externalObb);

            // Smart OBB Passthrough for High-Performance Gaming:
            // If the host device has OBB files for this game under /storage/emulated/<userId>/Android/obb/<pkg>
            // and the virtual OBB folder is empty, passthrough directly to the real host OBB folder for zero-copy native I/O speed!
            File hostObbPkg = new File(String.format("/storage/emulated/%d/Android/obb/%s", systemUserId, packageName));
            File[] virtualObbs = externalObb.listFiles();
            boolean hasVirtualObb = virtualObbs != null && virtualObbs.length > 0;
            if (!hasVirtualObb && hostObbPkg.exists()) {
                blackRule.add("/sdcard/Android/obb/" + packageName);
                blackRule.add(String.format("/storage/emulated/%d/Android/obb/%s", systemUserId, packageName));
            }

            rule.put("/sdcard", external.getAbsolutePath());
            rule.put(String.format("/storage/emulated/%d", systemUserId), external.getAbsolutePath());

            blackRule.add("/sdcard/Pictures");
            blackRule.add(String.format("/storage/emulated/%d/Pictures", systemUserId));
            blackRule.add("/sdcard/DCIM");
            blackRule.add(String.format("/storage/emulated/%d/DCIM", systemUserId));

            if (BlackBoxCore.get().isHideRoot()) {
                hideRoot(rule);
            }
            proc(rule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String s : blackRule) {
            get().addBlackRedirect(s);
        }
        for (String key : rule.keySet()) {
            get().addRedirect(key, rule.get(key));
        }
        NativeCore.enableIO();
        // Seccomp disabled to prevent SIGSYS/abort on guest process
    }

    private void hideRoot(Map<String, String> rule) {
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
    }

    private void proc(Map<String, String> rule) {
        int appPid = BlackBoxCore.getAppPid();
        int pid = Process.myPid();
        String selfProc = "/proc/self/";
        String proc = "/proc/" + pid + "/";

        String cmdline = new File(BEnvironment.getProcDir(appPid), "cmdline").getAbsolutePath();
        rule.put(proc + "cmdline", cmdline);
        rule.put(selfProc + "cmdline", cmdline);
    }
}


