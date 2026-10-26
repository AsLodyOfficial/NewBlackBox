package top.niunaijun.blackbox.fake.frameworks;

public class BLocationManager {
    public static final int CLOSE_MODE = 0;
    public static final int GLOBAL_MODE = 1;
    public static final int OWN_MODE = 2;

    public static io.vanta.fake.frameworks.BLocationManager get() {
        return io.vanta.fake.frameworks.BLocationManager.get();
    }

    public static boolean isFakeLocationEnable() {
        return io.vanta.fake.frameworks.BLocationManager.isFakeLocationEnable();
    }

    public static void disableFakeLocation(int userId, String pkg) {
        io.vanta.fake.frameworks.BLocationManager.disableFakeLocation(userId, pkg);
    }
}
