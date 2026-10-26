package top.niunaijun.blackbox.fake.frameworks;

import top.niunaijun.blackbox.entity.location.BLocation;

public class BLocationManager {
    public static final int CLOSE_MODE = com.mundo.fake.frameworks.BLocationManager.CLOSE_MODE;
    public static final int GLOBAL_MODE = com.mundo.fake.frameworks.BLocationManager.GLOBAL_MODE;
    public static final int OWN_MODE = com.mundo.fake.frameworks.BLocationManager.OWN_MODE;

    private static final BLocationManager sInstance = new BLocationManager();

    public static BLocationManager get() {
        return sInstance;
    }

    public static boolean isFakeLocationEnable() {
        return com.mundo.fake.frameworks.BLocationManager.isFakeLocationEnable();
    }

    public static void disableFakeLocation(int userId, String pkg) {
        com.mundo.fake.frameworks.BLocationManager.disableFakeLocation(userId, pkg);
    }

    public void setPattern(int userId, String pkg, int pattern) {
        com.mundo.fake.frameworks.BLocationManager.get().setPattern(userId, pkg, pattern);
    }

    public int getPattern(int userId, String pkg) {
        return com.mundo.fake.frameworks.BLocationManager.get().getPattern(userId, pkg);
    }

    public void setLocation(int userId, String pkg, BLocation location) {
        com.mundo.fake.frameworks.BLocationManager.get().setLocation(userId, pkg, location);
    }

    public BLocation getLocation(int userId, String pkg) {
        com.mundo.entity.location.BLocation loc = com.mundo.fake.frameworks.BLocationManager.get().getLocation(userId, pkg);
        if (loc == null) return null;
        if (loc instanceof BLocation) {
            return (BLocation) loc;
        }
        return new BLocation(loc.getLatitude(), loc.getLongitude());
    }
}
