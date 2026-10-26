package net_62v.external;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class MetaActivityManager {

    /**
     * Checks if the virtual application is currently running.
     * Called by MainActivity to verify process state.
     */
    public static boolean isAppRunning(String packageName) {
        return MundoManager.isAppRunning(packageName, 0);
    }

    /**
     * Launches the virtual application.
     * Called by MainActivity when the user clicks "StartGame".
     */
    public static void launchApp(String packageName) {
        MundoManager.launchApk(packageName, 0);
    }

    /**
     * Force-stops the virtual application.
     * Called by MainActivity when the user clicks "StopGame".
     */
    public static void killAppByPkg(String packageName) {
        MundoManager.stopPackage(packageName, 0);
    }
}
