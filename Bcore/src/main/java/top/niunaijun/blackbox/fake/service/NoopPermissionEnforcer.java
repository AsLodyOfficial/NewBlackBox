package top.niunaijun.blackbox.fake.service;

import android.os.PermissionEnforcer;
import android.util.Log;

public class NoopPermissionEnforcer extends PermissionEnforcer {
    private static final String TAG = "NoopPermissionEnforcer";

    public NoopPermissionEnforcer() {
        super();
    }

    @Override
    public void enforcePermission(String permission, int pid, int uid) {
        Log.d(TAG, "enforcePermission bypassed: " + permission + " for pid=" + pid + " uid=" + uid);
    }

    @Override
    public void enforcePermission(String permission, android.content.AttributionSource source) {
        Log.d(TAG, "enforcePermission (AttributionSource) bypassed: " + permission);
    }

    @Override
    protected int checkPermission(String permission, int pid, int uid) {
        Log.d(TAG, "checkPermission bypassed: " + permission + " for pid=" + pid + " uid=" + uid);
        return 0; // PERMISSION_GRANTED
    }

    @Override
    protected int checkPermission(String permission, android.content.AttributionSource source) {
        Log.d(TAG, "checkPermission (AttributionSource) bypassed: " + permission);
        return 0; // PERMISSION_GRANTED
    }
}
