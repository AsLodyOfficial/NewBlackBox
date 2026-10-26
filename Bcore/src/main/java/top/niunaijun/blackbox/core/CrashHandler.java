package top.niunaijun.blackbox.core;

import android.os.Looper;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.Slog;


public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private final Thread.UncaughtExceptionHandler mDefaultHandler;

    public static void create() {
        new CrashHandler();
    }

    public CrashHandler() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            if (BlackBoxCore.get().getExceptionHandler() != null) {
                BlackBoxCore.get().getExceptionHandler().uncaughtException(t, e);
            }
        } catch (Throwable ignored) {
        }

        // Anti-Crash Shield: Prevent non-fatal background thread exceptions (e.g. telemetry, GMS, ad SDKs, binder checks)
        // from terminating the entire virtualized game process.
        Thread mainThread = Looper.getMainLooper() != null ? Looper.getMainLooper().getThread() : null;
        if (t != mainThread && !(e instanceof OutOfMemoryError) && !(e instanceof StackOverflowError)) {
            Slog.w(TAG, "CrashShield intercepted background thread exception on [" + t.getName() + "]: " + e);
            return;
        }

        // If on main thread, check if it's a recoverable SecurityException / RemoteException / IllegalStateException from virtual hooks
        if (t == mainThread && isRecoverableMainException(e)) {
            Slog.w(TAG, "CrashShield recovered main thread exception: " + e);
            while (true) {
                try {
                    Looper.loop();
                } catch (Throwable loopErr) {
                    if (t == mainThread && isRecoverableMainException(loopErr)) {
                        Slog.w(TAG, "CrashShield recovered subsequent main loop exception: " + loopErr);
                        continue;
                    }
                    if (mDefaultHandler != null) {
                        mDefaultHandler.uncaughtException(t, loopErr);
                    }
                    return;
                }
            }
        }

        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        }
    }

    private boolean isRecoverableMainException(Throwable e) {
        if (e == null) return false;
        if (e instanceof SecurityException || e instanceof android.os.BadParcelableException) {
            return true;
        }
        String msg = e.getMessage() != null ? e.getMessage() : "";
        if (msg.contains(" Calling uid ") || msg.contains("Package ") || msg.contains("DeadSystemException")
                || msg.contains("GooglePlayServices") || msg.contains(" ContentProvider ") || msg.contains("Service not registered")) {
            return true;
        }
        Throwable cause = e.getCause();
        if (cause != null && cause != e) {
            return isRecoverableMainException(cause);
        }
        return false;
    }
}
