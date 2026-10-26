package top.niunaijun.blackbox.utils;

import java.io.File;

public class AbiUtils {
    public static boolean isSupport(File apkFile) {
        return com.mundo.utils.AbiUtils.isSupport(apkFile);
    }
}
