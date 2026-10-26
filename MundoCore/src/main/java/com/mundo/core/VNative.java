package com.mundo.core;

import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import androidx.annotation.Keep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mundo.MundoCore;
import com.mundo.app.BActivityThread;
import com.mundo.utils.compat.DexFileCompat;

public class VNative {

    public static final String TAG = "VNative";
    private static volatile boolean sCoreLoaded = false;
    private static volatile boolean sTargetLoaded = false;
    private static final Object sLock = new Object();
    public static String libtarget = "libbgmi.so";

    // ── Native library loading ────────────────────────────────────────────
    static {
        try {
            Context ctx = MundoCore.getContext();
            File coreLib = new File(
                ctx.getApplicationInfo().nativeLibraryDir,
                "libMundoCore.so"
            );
            if (coreLib.exists()) {
                System.load(coreLib.getAbsolutePath());
            } else {
                System.loadLibrary("MundoCore");
            }
            sCoreLoaded = true;

            // If detection features are in a separate HASAD lib, add:
            // System.loadLibrary("HASAD");   // or the full path

            // Load target lib silently (original behaviour)
            loadTargetSilently(ctx);
        } catch (Throwable t) {
            // silent
        }
    }

    private static void loadTargetSilently(Context ctx) {
        if (sTargetLoaded) return;
        synchronized (sLock) {
            if (sTargetLoaded) return;
            try {
                File targetLib = new File(ctx.getFilesDir(), "loader/" + libtarget);
                if (!targetLib.exists()) return;

                File optDir = new File(ctx.getFilesDir(), "loader/.opt");
                if (!optDir.exists()) optDir.mkdirs();

                ClassLoader parent = VNative.class.getClassLoader();
                Class<?> baseDexCL = Class.forName("dalvik.system.BaseDexClassLoader");
                Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");

                Field pathListField = baseDexCL.getDeclaredField("pathList");
                pathListField.setAccessible(true);
                Object pathList = pathListField.get(parent);

                Field nativeLibDirsField = dexPathListClass.getDeclaredField("nativeLibraryDirectories");
                nativeLibDirsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<File> nativeLibDirs = (List<File>) nativeLibDirsField.get(pathList);

                List<File> newDirs = new ArrayList<>();
                newDirs.add(targetLib.getParentFile());
                newDirs.addAll(nativeLibDirs);
                nativeLibDirsField.set(pathList, newDirs);

                // Patch nativeLibraryPathElements for API 23+
                try {
                    Field elementsField = dexPathListClass.getDeclaredField("nativeLibraryPathElements");
                    elementsField.setAccessible(true);
                    Method makeElements;
                    try {
                        makeElements = dexPathListClass.getDeclaredMethod("makePathElements", List.class);
                    } catch (NoSuchMethodException e) {
                        makeElements = dexPathListClass.getDeclaredMethod("makePathElements", List.class, File.class, List.class);
                    }
                    makeElements.setAccessible(true);
                    Object[] newElements;
                    try {
                        newElements = (Object[]) makeElements.invoke(null, newDirs);
                    } catch (Exception e) {
                        newElements = (Object[]) makeElements.invoke(null, newDirs, ctx.getFilesDir(), new ArrayList<>());
                    }
                    elementsField.set(pathList, newElements);
                } catch (Throwable ignored) {}

                System.loadLibrary(libtarget.replace("lib", "").replace(".so", ""));
                sTargetLoaded = true;
            } catch (Throwable t) {}
        }
    }

    // ── Original JNI declarations (MundoCore) ─────────────────────────────
    public static native void init(int apiLevel);
    public static native void enableIO();
    public static native void addIORule(String targetPath, String relocatePath);
    public static native void hideXposed();
    public static native void setHostPkg(String pkg);
    public static native void nukeTargetLibrary(String libName, int layers);

    // Nuke constants
    public static final int NUKE_L4_ELF_DESTROY = 1 << 3;



    // ── High‑level init combining host info + anti‑detection + IO rules ────
    public static void initDetectionAndNuke(int apiLevel) {
        // 1. Original host package info
        initHostInfo();

        // 2. Anti‑cheat nuke (matching NativeCore)
        try {
            // nukeTargetLibrary("libanogs", NUKE_L4_ELF_DESTROY);
            // nukeTargetLibrary("libanort", NUKE_L4_ELF_DESTROY);
            // Log.i(TAG, "NUKE sent for libanogs");
        } catch (Throwable t) {
            Log.e(TAG, "NUKE init failed", t);
        }

        // 3. Fix Vivo Android 16 VSPA frame drops by sinking the proc file to /dev/null
        try {
            addIORule("/proc/vivo_rsc/frame_set_priority", "/dev/null");
        } catch (Throwable t) {
            Log.e(TAG, "VSPA IO rule failed", t);
        }

        // 4. Redirect host data dir to guest
        applyGuestHostDataRedirect();
    }

    /**
     * Redirect legacy host data paths to the guest's private directory
     * so that games don't see the host's data.
     */
    private static void applyGuestHostDataRedirect() {
        try {
            String guestPkg = BActivityThread.getAppPackageName();
            if (guestPkg == null || guestPkg.isEmpty()) return;
            Context ctx = MundoCore.getContext();
            if (ctx == null) return;
            String hostPkg = MundoCore.getHostPkg();
            android.content.pm.ApplicationInfo guestInfo =
                    MundoCore.getBPackageManager()
                            .getApplicationInfo(guestPkg, 0, BActivityThread.getUserId());
            if (guestInfo == null || guestInfo.dataDir == null) return;
            String guestData = guestInfo.dataDir;
            addIORule("/data/user/0/" + hostPkg, guestData);
            addIORule("/data/data/" + hostPkg, guestData);
            if (!"com.pubgm".equals(hostPkg)) {
                addIORule("/data/user/0/com.pubgm", guestData);
                addIORule("/data/data/com.pubgm", guestData);
            }
            Log.i(TAG, "IO guest data redirect " + hostPkg + " -> " + guestData);
        } catch (Exception e) {
            Log.e(TAG, "IO rule failed", e);
        }
    }

    // ── Original helpers ──────────────────────────────────────────────────
    public static void initHostInfo() {
        try {
            String hostPkg = MundoCore.getHostPkg();
            if (hostPkg != null) {
                setHostPkg(hostPkg);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to set host pkg in native layer", e);
        }
    }

    @Keep
    public static int getCallingUid(int origCallingUid) {
        if (origCallingUid > 0 && origCallingUid < Process.FIRST_APPLICATION_UID) return origCallingUid;
        if (origCallingUid > Process.LAST_APPLICATION_UID) return origCallingUid;
        if (origCallingUid == MundoCore.getHostUid()) {
            // GMS / GSF packages: return host UID so Binder identity is consistent
            // across all Android versions (returning ROOT_UID breaks newer GMS modules).
            String appPkg = BActivityThread.getAppPackageName();
            if ("com.google.android.gms".equals(appPkg)
                    || "com.google.android.gsf".equals(appPkg)
                    || "com.google.process.gservices".equals(appPkg)
                    || "com.google.android.gms.persistent".equals(appPkg)) {
                return origCallingUid;  // pass through real host UID
            }
            if ("com.google.android.webview".equals(appPkg)) {
                return Process.myUid();
            }

            // Fix AttributionSource.enforceCallingUid crashes for Sandbox IPC
            if (android.os.Binder.getCallingPid() != android.os.Process.myPid()) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                int limit = Math.min(stack.length, 7);
                for (int i = 2; i < limit; i++) {
                    if (stack[i].getClassName().contains("AttributionSource")) {
                        return origCallingUid;
                    }
                }
            }

            return BActivityThread.getCallingBUid();
        }
        return origCallingUid;
    }

    @Keep
    public static String redirectPath(String path) {
        return VCore.get().redirectPath(path);
    }

    @Keep
    public static File redirectPath(File path) {
        return VCore.get().redirectPath(path);
    }


}