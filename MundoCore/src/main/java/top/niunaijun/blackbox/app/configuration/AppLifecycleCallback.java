package top.niunaijun.blackbox.app.configuration;

public class AppLifecycleCallback extends com.mundo.app.configuration.AppLifecycleCallback {
    public boolean onStoragePermissionNeeded(String packageName, int userId) {
        return false;
    }
}
