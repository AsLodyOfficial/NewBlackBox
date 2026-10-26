package top.niunaijun.blackbox.utils.compat;

import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Android 16 intent-redirection compatibility helper.
 */
public final class IntentRedirectCompat {
    private static final int ANDROID_16_API = 36;

    private static volatile boolean sCollectorResolved;
    private static volatile Method sCollectExtraIntentKeys;

    private IntentRedirectCompat() {
    }

    public static void collectNestedIntentKeys(Intent intent) {
        if (intent == null || Build.VERSION.SDK_INT < ANDROID_16_API) {
            return;
        }

        try {
            Method collector = getCollector();
            if (collector != null) {
                collector.invoke(intent);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Method getCollector() {
        if (sCollectorResolved) {
            return sCollectExtraIntentKeys;
        }

        synchronized (IntentRedirectCompat.class) {
            if (sCollectorResolved) {
                return sCollectExtraIntentKeys;
            }
            try {
                Method method = Intent.class.getDeclaredMethod("collectExtraIntentKeys");
                method.setAccessible(true);
                sCollectExtraIntentKeys = method;
            } catch (Throwable ignored) {
                sCollectExtraIntentKeys = null;
            } finally {
                sCollectorResolved = true;
            }
            return sCollectExtraIntentKeys;
        }
    }
}
