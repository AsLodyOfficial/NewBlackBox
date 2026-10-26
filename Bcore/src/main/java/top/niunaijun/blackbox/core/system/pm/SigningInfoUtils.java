package top.niunaijun.blackbox.core.system.pm;

import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import java.lang.reflect.Field;
import top.niunaijun.blackbox.utils.Slog;

public class SigningInfoUtils {
    private static final String TAG = "SigningInfoUtils";

    public static SigningInfo createSigningInfo(Signature[] signatures) {
        if (signatures == null || signatures.length == 0 || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return null;
        }
        try {
            SigningInfo signingInfo = new SigningInfo();
            Field detailsField = null;
            for (Field f : SigningInfo.class.getDeclaredFields()) {
                if (f.getType().getName().contains("SigningDetails")) {
                    detailsField = f;
                    break;
                }
            }
            if (detailsField != null) {
                detailsField.setAccessible(true);
                Object details = detailsField.get(signingInfo);
                if (details != null) {
                    for (Field f : details.getClass().getDeclaredFields()) {
                        if (f.getType().isArray() && f.getType().getComponentType() == Signature.class) {
                            f.setAccessible(true);
                            f.set(details, signatures);
                        }
                    }
                    return signingInfo;
                }
            }
        } catch (Throwable t) {
            Slog.w(TAG, "Failed to create SigningInfo via reflection: " + t.getMessage());
        }
        return null;
    }
}
