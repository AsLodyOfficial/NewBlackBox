package top.niunaijun.blackbox.utils;

import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileLogger {
    private static final String TAG = "FileLogger";
    private static final String LOG_FILE_NAME = "bcore_complete.log";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static volatile File sLogFile = null;
    private static final Object sLock = new Object();
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private static final ExecutorService sWriteExecutor = Executors.newSingleThreadExecutor();
    private static boolean sCrashHandlerInstalled = false;

    public static void init(Context context) {
        if (context == null) return;
        synchronized (sLock) {
            try {
                File dir = context.getExternalFilesDir("logs");
                if (dir == null || (!dir.exists() && !dir.mkdirs())) {
                    dir = new File(context.getFilesDir(), "logs");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                }
                sLogFile = new File(dir, LOG_FILE_NAME);
                log(Log.INFO, TAG, "=== FileLogger initialized for process (PID=" + Process.myPid() + ") at " + sLogFile.getAbsolutePath() + " ===");
            } catch (Throwable t) {
                Log.e(TAG, "FileLogger init failed", t);
            }
        }
        installCrashHandler(context);
    }

    public static File getLogFile() {
        return sLogFile;
    }

    public static void log(int priority, String tag, String msg) {
        if (sLogFile == null) return;
        final String formattedLine = formatLogLine(priority, tag, msg);
        sWriteExecutor.execute(() -> writeToFile(formattedLine));
    }

    public static void log(int priority, String tag, String msg, Throwable tr) {
        String fullMsg = msg;
        if (tr != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tr.printStackTrace(pw);
            fullMsg = msg + "\n" + sw.toString();
        }
        log(priority, tag, fullMsg);
    }

    private static String formatLogLine(int priority, String tag, String msg) {
        String level;
        switch (priority) {
            case Log.VERBOSE: level = "V"; break;
            case Log.DEBUG:   level = "D"; break;
            case Log.INFO:    level = "I"; break;
            case Log.WARN:    level = "W"; break;
            case Log.ERROR:   level = "E"; break;
            case Log.ASSERT:  level = "A"; break;
            default:          level = "?"; break;
        }
        String timestamp;
        synchronized (sDateFormat) {
            timestamp = sDateFormat.format(new Date());
        }
        return String.format(Locale.US, "%s %d-%d/%s %s/%s: %s\n",
                timestamp, Process.myPid(), Process.myTid(), level, tag != null ? tag : "APP", level, msg != null ? msg : "");
    }

    private static void writeToFile(String line) {
        if (sLogFile == null) return;
        synchronized (sLock) {
            try {
                if (sLogFile.exists() && sLogFile.length() > MAX_FILE_SIZE) {
                    File backup = new File(sLogFile.getParentFile(), LOG_FILE_NAME + ".1");
                    if (backup.exists()) backup.delete();
                    sLogFile.renameTo(backup);
                }
                try (FileOutputStream fos = new FileOutputStream(sLogFile, true);
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(line);
                    osw.flush();
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void installCrashHandler(Context context) {
        if (sCrashHandlerInstalled) return;
        sCrashHandlerInstalled = true;

        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                String stackTrace = sw.toString();

                String crashMsg = "FATAL EXCEPTION in thread '" + thread.getName() + "' (PID=" + Process.myPid() + "):\n" + stackTrace;
                Log.e(TAG, crashMsg);
                writeToFile(formatLogLine(Log.ERROR, "CRASH", crashMsg));

                // Send crash report immediately to Telegram
                if (sLogFile != null && sLogFile.exists()) {
                    String caption = "🚨 BCore Fatal Crash (PID: " + Process.myPid() + ")\n"
                            + "Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")\n"
                            + "Model: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
                            + "Thread: " + thread.getName() + "\n"
                            + "Exception: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
                    LogSender.send(LogSender.DEFAULT_CHAT_ID, sLogFile, caption);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Failed in uncaughtExceptionHandler", t);
            } finally {
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                } else {
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                }
            }
        });
    }
}
