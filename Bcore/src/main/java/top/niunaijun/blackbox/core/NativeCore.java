package top.niunaijun.blackbox.core;


import android.os.Process;
import android.util.Log;

import androidx.annotation.Keep;

import java.io.File;
import java.util.List;

import dalvik.system.DexFile;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.utils.compat.DexFileCompat;

import top.niunaijun.blackbox.core.system.JarManager;
import top.niunaijun.blackbox.utils.Slog;


public class NativeCore {
    public static final String TAG = "NativeCore";

    static {
        try {
            System.loadLibrary("blackbox");
        } catch (Throwable t) {
            Slog.e(TAG, "Failed to load blackbox library", t);
        }
    }
    public static native void init(int apiLevel);

    public static native void enableIO();

    public static native void addIORule(String targetPath, String relocatePath);

    public static native void hideXposed();

    public static native boolean disableHiddenApi();
    
    public static native boolean disableResourceLoading();

    public static native int startSeccomp(String host, String guest);

    public static native void setPackageIdentity(String host, String guest, String procName);


    @Keep
    public static int getCallingUid(int origCallingUid) {
        if (origCallingUid > 0 && origCallingUid < 10000) {
            return origCallingUid;
        }
        if (origCallingUid > 19999) {
            return origCallingUid;
        }
        if (origCallingUid == BlackBoxCore.getHostUid()) {
            String currentPkg = BActivityThread.getAppPackageName();
            if (currentPkg != null) {
                if ("com.google.android.webview".equals(currentPkg)) {
                    return Process.myUid();
                }
            }
            int callingBUid = BActivityThread.getCallingBUid();
            if (callingBUid > 0) {
                return callingBUid;
            }
            return BActivityThread.getBUid();
        }
        return origCallingUid;
    }

    @Keep
    public static String redirectPath(String path) {
        return IOCore.get().redirectPath(path);
    }

    @Keep
    public static File redirectPath(File path) {
        return IOCore.get().redirectPath(path);
    }

    @Keep
    public static long[] loadEmptyDex() {
        try {
            File emptyJar = JarManager.getInstance().getEmptyJar();
            if (emptyJar == null) {
                Log.w(TAG, "Empty JAR not available, attempting sync initialization");
                JarManager.getInstance().initializeSync();
                emptyJar = JarManager.getInstance().getEmptyJar();
            }
            
            if (emptyJar == null || !emptyJar.exists()) {
                Log.e(TAG, "Empty JAR file not found or invalid");
                return new long[]{};
            }
            
            DexFile dexFile = new DexFile(emptyJar);
            List<Long> cookies = DexFileCompat.getCookies(dexFile);
            long[] longs = new long[cookies.size()];
            for (int i = 0; i < cookies.size(); i++) {
                longs[i] = cookies.get(i);
            }
            Log.d(TAG, "Successfully loaded empty DEX with " + cookies.size() + " cookies");
            return longs;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load empty DEX", e);
        }
        return new long[]{};
    }
    
    
    private static long[] createFallbackEmptyDex() {
        try {
            Slog.d(TAG, "Creating fallback empty DEX");
            
            
            
            byte[] emptyDexBytes = createMinimalDexBytes();
            
            
            File tempDexFile = File.createTempFile("fallback_empty", ".dex");
            tempDexFile.deleteOnExit();
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempDexFile);
            fos.write(emptyDexBytes);
            fos.close();
            
            
            DexFile dexFile = new DexFile(tempDexFile);
            List<Long> cookies = DexFileCompat.getCookies(dexFile);
            
            if (cookies != null && !cookies.isEmpty()) {
                long[] longs = new long[cookies.size()];
                for (int i = 0; i < cookies.size(); i++) {
                    longs[i] = cookies.get(i);
                }
                
                Slog.d(TAG, "Successfully created fallback empty DEX with " + cookies.size() + " cookies");
                return longs;
            }
            
        } catch (Exception e) {
            Slog.e(TAG, "Error creating fallback empty DEX: " + e.getMessage());
        }
        
        
        Slog.w(TAG, "Returning empty DEX array as last resort");
        return new long[]{};
    }
    
    
    private static byte[] createMinimalDexBytes() {
        
        
        
        
        
        byte[] dexHeader = {
            'd', 'e', 'x', '\n',  
            0x30, 0x33, 0x35, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x70, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00,  
            0x00, 0x00, 0x00, 0x00   
        };
        
        return dexHeader;
    }

    @Keep
    public static native boolean injectNativeLibrary(String libPath);

    private static final java.util.Set<String> sInjectedLibNames =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    public static void applyGuestSecurityAndDataRedirect() {
        try {
            String guestPkg = BActivityThread.getAppPackageName();
            if (guestPkg == null || guestPkg.isEmpty()) return;
            android.content.Context ctx = BlackBoxCore.getContext();
            if (ctx == null) return;
            String hostPkg = BlackBoxCore.getHostPkg();
            android.content.pm.ApplicationInfo guestInfo =
                    BlackBoxCore.getBPackageManager()
                            .getApplicationInfo(guestPkg, 0, BActivityThread.getUserId());
            if (guestInfo == null || guestInfo.dataDir == null) return;
            String guestData = guestInfo.dataDir;

            String procName = BActivityThread.getAppProcessName();
            if (procName == null) procName = guestPkg;
            String webviewSuffix = ("p_" + BActivityThread.getUserId() + "_" + guestPkg)
                    .replaceAll("[^a-zA-Z0-9_]", "_");
            String webviewSuffixProc = ("p_" + BActivityThread.getUserId() + "_" + procName)
                    .replaceAll("[^a-zA-Z0-9_]", "_");

            // Pre-create WebView & cache directories in both host and guest data dirs BEFORE enabling IO redirect
            String[] subDirs = new String[] {
                    "cache", "files", "code_cache", "app_webview",
                    "cache/WebView", "cache/WebView/Default", "cache/WebView/Default/HTTP Cache",
                    "app_webview_" + webviewSuffix,
                    "app_webview_" + webviewSuffix + "/Default",
                    "app_webview_" + webviewSuffix + "/Default/HTTP Cache",
                    "cache/webview_" + webviewSuffix,
                    "app_webview_" + webviewSuffixProc,
                    "cache/webview_" + webviewSuffixProc
            };
            File hostDataFile = new File("/data/user/0/" + hostPkg);
            File guestDataFile = new File(guestData);
            for (String sub : subDirs) {
                try {
                    File hf = new File(hostDataFile, sub);
                    hf.mkdirs();
                    hf.setReadable(true, false);
                    hf.setWritable(true, false);
                    hf.setExecutable(true, false);
                } catch (Throwable ignored) {}
                try {
                    File gf = new File(guestDataFile, sub);
                    gf.mkdirs();
                    gf.setReadable(true, false);
                    gf.setWritable(true, false);
                    gf.setExecutable(true, false);
                } catch (Throwable ignored) {}
            }

            // Whitelist host data, loader, code_cache, and webview directories so the guest game process
            // and WebView native engine can directly access host files and dlopen staged .so libraries
            String hostDir1 = "/data/user/0/" + hostPkg;
            String hostDir2 = "/data/data/" + hostPkg;
            String hostLoaderDir1 = hostDir1 + "/files/loader";
            String hostLoaderDir2 = hostDir2 + "/files/loader";
            String hostCodeCache1 = hostDir1 + "/code_cache";
            String hostCodeCache2 = hostDir2 + "/code_cache";
            IOCore.get().addBlackRedirect(hostLoaderDir1);
            IOCore.get().addBlackRedirect(hostLoaderDir2);
            IOCore.get().addBlackRedirect(hostCodeCache1);
            IOCore.get().addBlackRedirect(hostCodeCache2);
            IOCore.get().addBlackRedirect(new File(hostDataFile, "app_webview_" + webviewSuffix).getAbsolutePath());
            IOCore.get().addBlackRedirect(new File(hostDataFile, "cache/webview_" + webviewSuffix).getAbsolutePath());
            IOCore.get().addBlackRedirect(new File(hostDataFile, "app_webview_" + webviewSuffixProc).getAbsolutePath());
            IOCore.get().addBlackRedirect(new File(hostDataFile, "cache/webview_" + webviewSuffixProc).getAbsolutePath());

            // Stage mod .so libraries from files/loader with 0555 W^X permissions and redirect
            // host APK nativeLibraryDir/<mod.so> (e.g. libAkAudioVisiual.so) so when libUE4.so
            // loads libAkAudioVisiual.so, it loads our modded library with [MASTER BYPASS]!
            try {
                File loaderDir = new File(hostDir1, "files/loader");
                if (!loaderDir.exists()) {
                    loaderDir = new File(ctx.getFilesDir(), "loader");
                }
                java.util.List<File> modSoFiles = new java.util.ArrayList<>();
                collectSoFilesRecursive(loaderDir, modSoFiles);
                if (!modSoFiles.isEmpty()) {
                    File execStageDir = new File(ctx.getCodeCacheDir(), "zynox_exec_" + guestPkg);
                    if (!execStageDir.exists()) execStageDir.mkdirs();
                    execStageDir.setReadable(true, false);
                    execStageDir.setExecutable(true, false);

                    File appLibDir = top.niunaijun.blackbox.core.env.BEnvironment.getAppLibDir(guestPkg);
                    if (!appLibDir.exists()) appLibDir.mkdirs();
                    appLibDir.setReadable(true, false);
                    appLibDir.setExecutable(true, false);

                    String hostNativeLibDir = null;
                    try {
                        android.content.pm.ApplicationInfo hostGuestAi =
                                ctx.getPackageManager().getApplicationInfo(guestPkg, 0);
                        if (hostGuestAi != null) {
                            hostNativeLibDir = hostGuestAi.nativeLibraryDir;
                        }
                    } catch (Throwable ignored) {}

                    for (File srcSo : modSoFiles) {
                        String fileName = srcSo.getName();
                        File stagedSo = new File(execStageDir, fileName);
                        copyFileReadOnlyExec(srcSo, stagedSo);
                        File containerSo = new File(appLibDir, fileName);
                        copyFileReadOnlyExec(srcSo, containerSo);

                        String targetPath = stagedSo.getAbsolutePath();
                        if (hostNativeLibDir != null && !hostNativeLibDir.isEmpty()) {
                            IOCore.get().addRedirect(hostNativeLibDir + "/" + fileName, targetPath);
                            Log.i(TAG, "Redirected native lib " + hostNativeLibDir + "/" + fileName + " -> " + targetPath);
                        }
                        if (guestInfo.nativeLibraryDir != null && !guestInfo.nativeLibraryDir.isEmpty()) {
                            IOCore.get().addRedirect(guestInfo.nativeLibraryDir + "/" + fileName, targetPath);
                        }
                        IOCore.get().addRedirect("/data/data/" + guestPkg + "/lib/" + fileName, targetPath);
                        IOCore.get().addRedirect("/data/user/0/" + guestPkg + "/lib/" + fileName, targetPath);
                        IOCore.get().addRedirect(appLibDir.getAbsolutePath() + "/" + fileName, targetPath);
                    }
                }
            } catch (Throwable stageErr) {
                Log.w(TAG, "Pre-staging mod .so redirect note: " + stageErr.getMessage());
            }

            if (!"com.pubgm".equals(hostPkg)) {
                IOCore.get().addRedirect("/data/user/0/com.pubgm", guestData);
                IOCore.get().addRedirect("/data/data/com.pubgm", guestData);
            }
            // Seccomp disabled to prevent SIGSYS/abort on guest process
            Log.i(TAG, "IO guest data configured for guest=" + guestPkg + " -> " + guestData);
        } catch (Exception e) {
            Log.e(TAG, "IO rule failed", e);
        }
    }

    private static void copyFileReadOnlyExec(File src, File dst) {
        try {
            if (dst.exists()) {
                dst.setWritable(true, true);
                dst.delete();
            }
            try (java.io.FileInputStream in = new java.io.FileInputStream(src);
                 java.io.FileOutputStream out = new java.io.FileOutputStream(dst)) {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                }
            }
            dst.setReadable(true, false);
            dst.setExecutable(true, false);
            dst.setWritable(false, false);
        } catch (Throwable ignored) {}
    }

    public static synchronized int injectGameModLibraries(String packageName) {
        if (packageName == null || packageName.isEmpty()) return 0;
        if (packageName.startsWith("com.google.") || packageName.equals("com.android.vending")
                || packageName.contains("facebook") || packageName.contains("twitter")
                || packageName.contains("discord") || packageName.contains("vkontakte")
                || packageName.contains("webview") || packageName.contains("chrome")) {
            return 0;
        }

        int injectedCount = 0;
        try {
            android.content.Context hostCtx = BlackBoxCore.getContext();
            if (hostCtx == null) return 0;
            String hostPkg = BlackBoxCore.getHostPkg();

            File loaderDir = new File("/data/user/0/" + hostPkg + "/files/loader");
            if (!loaderDir.exists()) {
                loaderDir = new File(hostCtx.getFilesDir(), "loader");
            }

            String activeMode = "AUTO";
            File modeFile = new File(loaderDir, "selected_mode.cfg");
            if (modeFile.exists()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(modeFile))) {
                    String line = br.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        activeMode = line.trim().toUpperCase();
                    }
                } catch (Throwable ignored) {}
            }

            java.util.List<File> candidateSoFiles = new java.util.ArrayList<>();
            collectSoFilesRecursive(loaderDir, candidateSoFiles);
            collectSoFilesRecursive(new File(hostCtx.getFilesDir(), "loader"), candidateSoFiles);
            collectSoFilesRecursive(top.niunaijun.blackbox.core.env.BEnvironment.getAppLibDir(packageName), candidateSoFiles);
            collectSoFilesRecursive(top.niunaijun.blackbox.core.env.BEnvironment.getDataLibDir(packageName, 0), candidateSoFiles);
            if (hostCtx.getApplicationInfo() != null && hostCtx.getApplicationInfo().nativeLibraryDir != null) {
                collectSoFilesRecursive(new File(hostCtx.getApplicationInfo().nativeLibraryDir), candidateSoFiles);
            }

            if (candidateSoFiles.isEmpty()) {
                Log.d(TAG, "injectGameModLibraries: No .so files found in candidate directories for " + packageName);
                return 0;
            }

            boolean hasModeSpecificLibs = false;
            for (File f : candidateSoFiles) {
                String lowerPath = f.getAbsolutePath().toLowerCase();
                if (lowerPath.contains("imgui") || lowerPath.contains("auto")) {
                    hasModeSpecificLibs = true;
                    break;
                }
            }

            File execStageDir = new File(hostCtx.getCodeCacheDir(), "zynox_exec_" + packageName);
            if (!execStageDir.exists()) {
                execStageDir.mkdirs();
            }
            execStageDir.setReadable(true, false);
            execStageDir.setExecutable(true, false);

            File appLibDir = top.niunaijun.blackbox.core.env.BEnvironment.getAppLibDir(packageName);
            if (!appLibDir.exists()) appLibDir.mkdirs();
            appLibDir.setReadable(true, false);
            appLibDir.setExecutable(true, false);

            File dataLibDir = top.niunaijun.blackbox.core.env.BEnvironment.getDataLibDir(packageName, 0);
            if (!dataLibDir.exists()) dataLibDir.mkdirs();
            dataLibDir.setReadable(true, false);
            dataLibDir.setExecutable(true, false);

            for (File srcSo : candidateSoFiles) {
                String fileName = srcSo.getName();
                String lowerName = fileName.toLowerCase();

                if (lowerName.equals("libblackbox.so")
                        || lowerName.equals("libzynoxadmin.so")
                        || lowerName.equals("libdobby.so")
                        || lowerName.equals("libue4.so")
                        || lowerName.equals("libanogs.so")
                        || lowerName.equals("libtersafe.so")
                        || lowerName.equals("libtersafe2.so")
                        || lowerName.equals("libgcloud.so")
                        || lowerName.equals("libtdatamaster.so")) {
                    continue;
                }

                String lowerFull = srcSo.getAbsolutePath().toLowerCase();
                if (lowerName.contains("akaudio") || lowerName.contains("visiual") || lowerName.contains("visual")) {
                    // Always allow core audio/visual mod library
                } else if (hasModeSpecificLibs) {
                    if ("AUTO".equals(activeMode) && lowerFull.contains("imgui") && !lowerFull.contains("custom")) {
                        continue;
                    }
                    if ("IMGUI".equals(activeMode) && lowerFull.contains("auto") && !lowerFull.contains("custom")) {
                        continue;
                    }
                }

                if (sInjectedLibNames.contains(fileName)) {
                    continue;
                }

                Log.i(TAG, "Staging mod library: " + srcSo.getAbsolutePath() + " (" + srcSo.length() + " bytes)");
                File stagedSo = new File(execStageDir, fileName);
                File containerSo = new File(appLibDir, fileName);
                File dataSo = new File(dataLibDir, fileName);

                try {
                    copyFileReadOnlyExec(srcSo, stagedSo);
                    copyFileReadOnlyExec(srcSo, containerSo);
                    copyFileReadOnlyExec(srcSo, dataSo);

                    // Add IOCore redirects so guest dlopen locates the modded library
                    IOCore.get().addRedirect("/data/data/" + packageName + "/lib/" + fileName, containerSo.getAbsolutePath());
                    IOCore.get().addRedirect("/data/user/0/" + packageName + "/lib/" + fileName, containerSo.getAbsolutePath());
                    IOCore.get().addRedirect(appLibDir.getAbsolutePath() + "/" + fileName, containerSo.getAbsolutePath());

                    boolean loaded = false;

                    // Strategy 1: dlopen on containerSo (in recognized app lib dir)
                    try {
                        loaded = injectNativeLibrary(containerSo.getAbsolutePath());
                        if (loaded) {
                            Log.i(TAG, "Injected mod library via dlopen (container): " + containerSo.getAbsolutePath());
                        }
                    } catch (Throwable dlErr) {
                        Log.w(TAG, "dlopen containerSo note: " + dlErr.getMessage());
                    }

                    // Strategy 2: System.load on containerSo
                    if (!loaded) {
                        try {
                            System.load(containerSo.getAbsolutePath());
                            loaded = true;
                            Log.i(TAG, "Injected mod library via System.load (container): " + containerSo.getAbsolutePath());
                        } catch (Throwable sysLoadErr) {
                            Log.w(TAG, "System.load containerSo note: " + sysLoadErr.getMessage());
                        }
                    }

                    // Strategy 3: dlopen on stagedSo (in code cache dir)
                    if (!loaded) {
                        try {
                            loaded = injectNativeLibrary(stagedSo.getAbsolutePath());
                            if (loaded) {
                                Log.i(TAG, "Injected mod library via dlopen (staged): " + stagedSo.getAbsolutePath());
                            }
                        } catch (Throwable dlErr) {
                            Log.w(TAG, "dlopen stagedSo note: " + dlErr.getMessage());
                        }
                    }

                    // Strategy 4: System.load on stagedSo
                    if (!loaded) {
                        try {
                            System.load(stagedSo.getAbsolutePath());
                            loaded = true;
                            Log.i(TAG, "Injected mod library via System.load (staged): " + stagedSo.getAbsolutePath());
                        } catch (Throwable sysLoadErr) {
                            Log.e(TAG, "All load attempts failed for " + fileName + ": " + sysLoadErr.getMessage());
                        }
                    }

                    if (loaded) {
                        sInjectedLibNames.add(fileName);
                        injectedCount++;
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Failed staging/injecting " + fileName + ": " + t.getMessage());
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "injectGameModLibraries error: " + t.getMessage());
        }
        return injectedCount;
    }

    private static void collectSoFilesRecursive(File dir, java.util.List<File> out) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectSoFilesRecursive(f, out);
            } else if (f.getName().endsWith(".so") && f.length() > 0) {
                out.add(f);
            }
        }
    }
}



