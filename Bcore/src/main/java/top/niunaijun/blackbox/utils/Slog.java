

package top.niunaijun.blackbox.utils;

import android.util.Log;


public final class Slog {
     public static final int LOG_ID_SYSTEM = 3;

    private Slog() {
    }

    public static int v(String tag, String msg) {
        FileLogger.log(Log.VERBOSE, tag, msg);
        return Log.println(Log.VERBOSE, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        FileLogger.log(Log.VERBOSE, tag, msg, tr);
        return Log.println(Log.VERBOSE, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    
    public static int d(String tag, String msg) {
        FileLogger.log(Log.DEBUG, tag, msg);
        return Log.println(Log.DEBUG, tag, msg);
    }

    
    public static int d(String tag, String msg, Throwable tr) {
        FileLogger.log(Log.DEBUG, tag, msg, tr);
        return Log.println(Log.DEBUG, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    
    public static int i(String tag, String msg) {
        FileLogger.log(Log.INFO, tag, msg);
        return Log.println(Log.INFO, tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        FileLogger.log(Log.INFO, tag, msg, tr);
        return Log.println(Log.INFO, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    
    public static int w(String tag, String msg) {
        FileLogger.log(Log.WARN, tag, msg);
        return Log.println(Log.WARN, tag, msg);
    }

    
    public static int w(String tag, String msg, Throwable tr) {
        FileLogger.log(Log.WARN, tag, msg, tr);
        return Log.println(Log.WARN, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int w(String tag, Throwable tr) {
        FileLogger.log(Log.WARN, tag, "", tr);
        return Log.println(Log.WARN, tag, Log.getStackTraceString(tr));
    }

    
    public static int e(String tag, String msg) {
        FileLogger.log(Log.ERROR, tag, msg);
        return Log.println(Log.ERROR, tag, msg);
    }

    
    public static int e(String tag, String msg, Throwable tr) {
        FileLogger.log(Log.ERROR, tag, msg, tr);
        return Log.println(Log.ERROR, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int println(int priority, String tag, String msg) {
        FileLogger.log(priority, tag, msg);
        return Log.println(priority, tag, msg);
    }
}

