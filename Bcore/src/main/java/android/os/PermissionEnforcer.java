package android.os;

import android.content.Context;

public class PermissionEnforcer {
    public PermissionEnforcer(Context context) {
    }

    public PermissionEnforcer() {
    }

    public void enforcePermission(String permission, int pid, int uid) throws SecurityException {
    }

    public void enforcePermission(String permission, android.content.AttributionSource source) throws SecurityException {
    }

    protected int checkPermission(String permission, int pid, int uid) {
        return 0;
    }

    protected int checkPermission(String permission, android.content.AttributionSource source) {
        return 0;
    }
}
